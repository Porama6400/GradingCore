package dev.porama.gradingcore.core;

import ch.qos.logback.classic.Level;
import dev.porama.gradingcore.core.config.MainConfiguration;
import dev.porama.gradingcore.core.config.TemplateService;
import dev.porama.gradingcore.core.grader.GraderService;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.rabbit.RabbitMessenger;
import dev.porama.gradingcore.core.metrics.MetricsManager;
import dev.porama.gradingcore.core.postprocessor.PostProcessorService;
import dev.porama.gradingcore.core.postprocessor.impl.MemoryLimitPostProcessor;
import dev.porama.gradingcore.core.postprocessor.impl.TimeLimitPostProcessor;
import dev.porama.gradingcore.core.scheduler.Scheduler;
import dev.porama.gradingcore.core.temp.TempFileService;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class GradingCore {
    private MainConfiguration mainConfiguration;
    private File tempDirectory;
    private Logger logger;
    private Messenger messenger;
    private TemplateService templateService;
    private GraderService graderService;
    private TempFileService tempFileService;
    private ScheduledExecutorService masterThreadPool;
    private PostProcessorService postProcessor;
    private MetricsManager metricsManager;
    private Scheduler scheduler;

    public void start() throws IOException {
        logger = LoggerFactory.getLogger(GradingCore.class);

        mainConfiguration = ConfigUtils.load(new File("config.json"), MainConfiguration.class);
        mainConfiguration.setHostname(mainConfiguration.getHostname().replaceAll("%hostname%", getHostName()));

        tempDirectory = new File("temp");
        tempFileService = new TempFileService(tempDirectory);
        masterThreadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        metricsManager = new MetricsManager(
                mainConfiguration.getInfluxUrl(), mainConfiguration.getInfluxToken(),
                mainConfiguration.getInfluxOrg(), mainConfiguration.getInfluxBucket(),
                mainConfiguration.getHostname());
        masterThreadPool.scheduleAtFixedRate(metricsManager::tickPublish, 5, 5, TimeUnit.SECONDS);
        logger.info("Starting GradingCore - node " + mainConfiguration.getHostname());
        if (mainConfiguration.isDebug()) {
            logger.info("Debug mode is enabled");
            //set log level to debug
            Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            if (rootLogger instanceof ch.qos.logback.classic.Logger logbackLogger) {
                logbackLogger.setLevel(Level.ALL);
            } else {
                logger.error("Unsupported logger type " + rootLogger.getClass().getName());
            }
        }

        templateService = new TemplateService();
        graderService = new GraderService(templateService, masterThreadPool, mainConfiguration);
        messenger = new RabbitMessenger(metricsManager, mainConfiguration.getMessengerUri(), mainConfiguration.getParallelism());
        postProcessor = new PostProcessorService();
        postProcessor.add(new MemoryLimitPostProcessor());
        postProcessor.add(new TimeLimitPostProcessor());
        scheduler = new Scheduler(graderService, masterThreadPool, postProcessor, mainConfiguration);

        messenger.listen(masterThreadPool, scheduler::handle);
    }

    public String getHostName() {
        try {
            Process hostname = Runtime.getRuntime().exec("hostname");
            hostname.waitFor();
            String hostString = new String(hostname.getInputStream().readAllBytes());
            hostString = hostString.replaceAll("(\r|\n)", "");
            return hostString;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void shutdown() {
        graderService.shutdown();
        messenger.shutdown();
        masterThreadPool.shutdown();
    }
}

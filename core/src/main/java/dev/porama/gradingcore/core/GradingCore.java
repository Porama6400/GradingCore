package dev.porama.gradingcore.core;

import ch.qos.logback.classic.Level;
import dev.porama.gradingcore.core.config.MainConfiguration;
import dev.porama.gradingcore.core.config.TemplateService;
import dev.porama.gradingcore.core.grader.GraderService;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.grader.data.GradingStatus;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.RequeueLimiter;
import dev.porama.gradingcore.core.messenger.rabbit.RabbitMessenger;
import dev.porama.gradingcore.core.metrics.MetricsManager;
import dev.porama.gradingcore.core.temp.TempFileService;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Getter
public class GradingCore {

    private RequeueLimiter requeueLimiter;
    private MainConfiguration mainConfiguration;
    private File tempDirectory;
    private Logger logger;
    private Messenger messenger;
    private TemplateService templateService;
    private GraderService graderService;
    private TempFileService tempFileService;
    private ScheduledExecutorService masterThreadPool;
    private MetricsManager metricsManager;
    private long nextTimeSlot = 0;

    public void start() throws IOException {
        mainConfiguration = ConfigUtils.load(new File("config.json"), MainConfiguration.class);
        tempDirectory = new File("temp");
        tempFileService = new TempFileService(tempDirectory);
        masterThreadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        requeueLimiter = new RequeueLimiter();
        masterThreadPool.scheduleAtFixedRate(requeueLimiter::resetAll, 15, 15, TimeUnit.MINUTES);
        metricsManager = new MetricsManager(
                mainConfiguration.getInfluxUrl(), mainConfiguration.getInfluxToken(),
                mainConfiguration.getInfluxOrg(), mainConfiguration.getInfluxBucket(),
                mainConfiguration.getNodeId());
        masterThreadPool.scheduleAtFixedRate(metricsManager::tickPublish, 5, 5, TimeUnit.SECONDS);

        logger = LoggerFactory.getLogger(GradingCore.class);
        logger.info("Starting GradingCore");
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

        graderService = new GraderService(templateService, masterThreadPool, mainConfiguration.getTickInterval());

        messenger = new RabbitMessenger(metricsManager, mainConfiguration.getMessengerUri(), mainConfiguration.getParallelism());
        masterThreadPool.execute(() -> {
            try {
                messenger.listen(req -> {
                    requeueLimiter.increment(req.getId());
                    if (requeueLimiter.hasExceeded(req.getId(), mainConfiguration.getMaxRequeue())) {
                        logger.warn("Submission {} has exceeded the maximum requeue limit", req.getId());
                        return CompletableFuture.completedFuture(new GradingResult(
                                req.getId(),
                                GradingStatus.REQUEUE_LIMIT_EXCEEDED
                        ));
                    }

                    synchronized (this) {
                        long currentTime = System.currentTimeMillis();
                        if (nextTimeSlot < currentTime) {
                            nextTimeSlot = currentTime;
                        }
                        long delay = nextTimeSlot - currentTime;
                        nextTimeSlot += getMainConfiguration().getTimeSlotWidth();

                        logger.info("Scheduled " + req.getId() + " to run " + delay + "ms in the future");
                        return delayingFuture(() -> graderService.submit(req), delay);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> CompletableFuture<T> delayingFuture(Supplier<CompletableFuture<T>> supplier, long delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        masterThreadPool.schedule(() -> {
            CompletableFuture<T> realFuture = supplier.get();
            realFuture.thenAccept(future::complete).exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        }, delay, TimeUnit.MILLISECONDS);
        return future;
    }

    public void shutdown() {
        graderService.shutdown();
        messenger.shutdown();
        masterThreadPool.shutdown();
    }
}

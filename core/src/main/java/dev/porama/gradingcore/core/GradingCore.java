package dev.porama.gradingcore.core;

import ch.qos.logback.classic.Level;
import dev.porama.gradingcore.core.config.MainConfiguration;
import dev.porama.gradingcore.core.config.TemplateService;
import dev.porama.gradingcore.core.grader.GraderService;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.rabbit.RabbitMessenger;
import dev.porama.gradingcore.core.temp.TempFileService;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Getter
public class GradingCore {

    private MainConfiguration mainConfiguration;
    private File tempDirectory;
    private Logger logger;
    private Messenger messenger;
    private TemplateService templateService;
    private GraderService graderService;
    private TempFileService tempFileService;

    public void start() throws IOException, InterruptedException {
        mainConfiguration = ConfigUtils.load(new File("config.json"), MainConfiguration.class);
        tempDirectory = new File("temp");
        tempFileService = new TempFileService(tempDirectory);

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

        graderService = new GraderService(templateService);

        messenger = new RabbitMessenger(mainConfiguration.getMessengerUri());
    }

    public void shutdown() {
        graderService.shutdown();
    }
}

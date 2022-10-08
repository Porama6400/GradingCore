package dev.porama.gradingcore;

import ch.qos.logback.classic.Level;
import dev.porama.gradingcore.config.MainConfiguration;
import dev.porama.gradingcore.config.TemplateService;
import dev.porama.gradingcore.container.BasicContainer;
import dev.porama.gradingcore.container.Container;
import dev.porama.gradingcore.container.data.ContainerTemplate;
import dev.porama.gradingcore.grader.GraderService;
import dev.porama.gradingcore.grader.data.GradingRequest;
import dev.porama.gradingcore.messenger.MessageHandler;
import dev.porama.gradingcore.messenger.Messenger;
import dev.porama.gradingcore.messenger.rabbit.RabbitMessenger;
import dev.porama.gradingcore.temp.TempFileService;
import dev.porama.gradingcore.utils.ConfigUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.ALL);
        }

        templateService = new TemplateService();

        messenger = new RabbitMessenger(mainConfiguration.getMessengerUri());

        graderService = new GraderService(templateService, messenger);
    }

    public void shutdown() {
        graderService.shutdown();
    }
}

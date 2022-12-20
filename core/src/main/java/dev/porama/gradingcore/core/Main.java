package dev.porama.gradingcore.core;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        GradingCore gradingCore = new GradingCore();
        gradingCore.start();

        logger.info("Press any key to shutdown...");
        System.in.read();
        gradingCore.shutdown();
    }
}
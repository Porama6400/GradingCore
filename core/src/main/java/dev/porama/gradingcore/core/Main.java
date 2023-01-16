package dev.porama.gradingcore.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        GradingCore gradingCore = new GradingCore();
        gradingCore.start();

        logger.info("Press any key to shutdown...");
        System.in.read();
        gradingCore.shutdown();
    }
}
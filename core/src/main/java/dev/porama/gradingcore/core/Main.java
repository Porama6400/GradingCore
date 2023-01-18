package dev.porama.gradingcore.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        GradingCore gradingCore = new GradingCore();
        gradingCore.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gradingCore.shutdown();
            } catch (Exception e) {
                logger.error("Failed to stop GradingCore", e);
            }
        }));
    }
}
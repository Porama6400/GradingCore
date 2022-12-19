package dev.porama.gradingcore.core;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        GradingCore gradingCore = new GradingCore();
        gradingCore.start();

//        AtomicInteger counter = new AtomicInteger();
//        for (int i = 0; i < 3; i++) {
//            submitTest(gradingCore, counter);
//        }
        logger.info("Press any key to shutdown...");
        System.in.read();
        gradingCore.shutdown();
    }

    public static void submitTest(GradingCore gradingCore, AtomicInteger counter) {

        String exampleRequest = """
                {
                  "submissionId": "abc123",
                  "type": "c",
                  "softLimitMemory": 1000,
                  "softLimitTime": 1000,
                  "filesSource": [
                    {
                      "name": "main.c",
                      "sourceType": "STRING",
                      "payload": "#include <stdio.h>\\nint main(){ printf(\\"Hello!\\"); }"
                    }
                  ]
                }
                """;
        GradingRequest request = ConfigUtils.fromJson(exampleRequest, GradingRequest.class);
        request.setSubmissionId(UUID.randomUUID().toString());

        counter.incrementAndGet();
        gradingCore.getGraderService().submit(request).thenAccept(result -> {
            System.out.println("Submission completed: " + result.getSubmissionId());
            int currentValue = counter.decrementAndGet();
            System.out.println("Still running: " + currentValue);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            int currentValue = counter.decrementAndGet();
            System.out.println("Still running: " + currentValue);
            return null;
        });
    }
}
package dev.porama.gradingcore.core;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.utils.ConfigUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        SeaweedConnector seaweedConnector = new SeaweedConnector(executorService);
//        seaweedConnector.postFile(new URI("http://localhost:8081/6,01048a9474"),
//                        "cat.mp4",
//                        StreamUtils.toBytes(new FileInputStream("/home/porama/Desktop/hello.txt")))
//                .thenAccept(System.out::println).join();
//        executorService.shutdown();


        GradingCore gradingCore = new GradingCore();
        gradingCore.start();
        String exampleRequest = """
                  {
                  "type": "c",
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
        GradingResult join = gradingCore.getGraderService().submit(request).join();
        System.out.println(ConfigUtils.toJson(join));
        gradingCore.shutdown();
    }
}
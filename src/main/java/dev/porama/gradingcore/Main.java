package dev.porama.gradingcore;

import dev.porama.gradingcore.file.SeaweedConnector;
import dev.porama.gradingcore.grader.data.GradingRequest;
import dev.porama.gradingcore.utils.ConfigUtils;
import dev.porama.gradingcore.utils.StreamUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
             "type": "java",
             "fileSources": [
               {
                 "name": "Main.java",
                 "sourceType": "BASE64",
                 "payload": "cHVibGljIGNsYXNzIE1haW57CiAgICBwdWJsaWMgc3RhdGljIHZvaWQgbWFpbihTdHJpbmdbXSBhcmdzKXsKICAgICAgICBTeXN0ZW0ub3V0LnByaW50bG4oIkhlbGxvIHdvcmxkISIpOwogICAgfQp9"
               }
             ]
           }
           
           """;
        GradingRequest request = ConfigUtils.fromJson(exampleRequest, GradingRequest.class);
        gradingCore.getGraderService().submit(request).join();
        gradingCore.shutdown();
    }
}
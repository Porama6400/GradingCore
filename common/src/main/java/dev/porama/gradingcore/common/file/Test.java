package dev.porama.gradingcore.common.file;

import dev.porama.gradingcore.common.seaweed.AllocationResponse;
import dev.porama.gradingcore.common.seaweed.SeaweedConnector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) throws URISyntaxException, IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SeaweedConnector seaweedConnector = new SeaweedConnector(executorService);
        TreeMap<String, String> parameters = new TreeMap<>();
        parameters.put("count", "5");

        URI masterUri = new URI("http://localhost:9333/");
        AllocationResponse response = seaweedConnector.allocateFile(masterUri, parameters).join();
        System.out.println(response.toString());
        System.out.println(masterUri.getScheme());
        URI fullUri = response.constructFullUri(false);
        System.out.println(fullUri);
        seaweedConnector.postFileUrl(fullUri, "test.txt", "test hello world! XD".getBytes()).join();
//
//        URI masterUri = new URI("http://localhost:9333/");
//        AllocationResponse response = seaweedConnector.uploadFile(masterUri, parameters, false, "test.txt", "test test test".getBytes()).join();
//        URI fileUri = response.constructFullUri(false);
//        System.out.println(fileUri);
//
//        byte[] received = seaweedConnector.getFile(fileUri).join();
//        System.out.println("Received: " + new String(received));

        byte[] data = seaweedConnector.downloadFile(masterUri, response.getFileId()).join();
        System.out.println(new String(data));

        executorService.shutdown();
    }
}

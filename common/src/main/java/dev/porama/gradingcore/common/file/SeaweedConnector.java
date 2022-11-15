package dev.porama.gradingcore.common.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

public class SeaweedConnector {

    private final HttpClient client;

    //    private String boundary = UUID.randomUUID().toString().replace("-", "");
    private String boundary = "------------------------9cfeeb31b6183786";

    public SeaweedConnector(ExecutorService executor) {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(executor)
                .build();
    }

    public static String randomHex(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(Integer.toHexString((int) (ThreadLocalRandom.current().nextDouble() * 16)));
        }
        return builder.toString();
    }

    public CompletableFuture<String> postFile(URI uri, String fileName, byte[] data) throws IOException {
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-_]", "");

        final String boundary = "------------------------" + randomHex(16);
        final byte[] boundaryBytes = boundary.getBytes();
        final byte[] doubleDash = "--".getBytes();
        final byte[] lineBreakBytes = "\r\n".getBytes();

        final String header = "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                        "Content-Type: application/octet-stream";

        ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length + 500);
        stream.write(doubleDash);
        stream.write(boundaryBytes);
        stream.write(lineBreakBytes);
        stream.write(header.getBytes());
        stream.write(lineBreakBytes);
        stream.write(lineBreakBytes);
        stream.write(data);
        stream.write(lineBreakBytes);
        stream.write(doubleDash);
        stream.write(boundaryBytes);
        stream.write(doubleDash);
        stream.write(lineBreakBytes);
        stream.flush();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(stream.toByteArray()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
    }
}

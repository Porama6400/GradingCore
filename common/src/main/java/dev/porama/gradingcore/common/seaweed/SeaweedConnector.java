package dev.porama.gradingcore.common.seaweed;

import com.google.gson.*;
import dev.porama.gradingcore.common.http.URIUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

public class SeaweedConnector {
    private final HttpClient client;
    private final URI masterUri;
    private final Gson gson = new GsonBuilder().create();
    private boolean useTls = false;

    public SeaweedConnector(ExecutorService executor) {
        this(executor, null);
    }

    public SeaweedConnector(ExecutorService executor, URI masterUri) {
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10)).executor(executor).build();
        this.masterUri = masterUri;
    }

    public static String randomHex(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(Integer.toHexString((int) (ThreadLocalRandom.current().nextDouble() * 16)));
        }
        return builder.toString();
    }

    public String getProtocolString() {
        return useTls ? "https" : "http";
    }

    public CompletableFuture<AllocationResponse> uploadFile(@Nullable Map<String, String> parameters, String fileName, byte[] data) {
        return allocateFile(parameters).thenCompose(response -> {
            try {
                return postFileUrl(
                        new URI(getProtocolString() + "://" + response.getPublicUrlString() + "/" + response.getFileId()),
                        fileName,
                        data
                ).thenApply(res -> response);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<byte[]> downloadFile(String token) {
        return locateFile(token).thenCompose(locateResponse -> {
            try {
                return getFileUrl(new URI(getProtocolString() + "://" + locateResponse.getPublicUrl() + "/" + token));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<AllocationResponse> allocateFile() {
        return allocateFile(null);
    }

    public CompletableFuture<AllocationResponse> allocateFile(@Nullable Map<String, String> parameters) {
        HttpRequest httpRequest = HttpRequest.newBuilder(masterUri.resolve("dir/assign" + URIUtils.serializeParameters(parameters))).GET().build();
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() / 100 != 2) {
                throw new RuntimeException("Server responded with " + response);
            }
            return gson.fromJson(response.body(), AllocationResponse.class);
        });
    }

    public CompletableFuture<LocateResponse> locateFile(String token) {
        HttpRequest request = HttpRequest.newBuilder(masterUri.resolve("dir/lookup?volumeId=" + token)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            String bodyString = response.body();
            JsonArray locations = JsonParser.parseString(bodyString).getAsJsonObject().getAsJsonArray("locations");
            if (locations.size() == 0) {
                throw new SeaweedException("Unable to locate");
            }
            JsonElement element = locations.get(0);
            return new LocateResponse(
                    element.getAsJsonObject().get("url").getAsString(),
                    element.getAsJsonObject().get("publicUrl").getAsString()
            );
        });
    }

    public CompletableFuture<byte[]> getFileUrl(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> postFileUrl(URI uri, String fileName, byte[] data) throws IOException {
//        fileName = fileName.replaceAll("[^a-zA-Z0-9.-_]", "");
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        final String boundary = "------------------------" + randomHex(16);
        final byte[] boundaryBytes = boundary.getBytes();
        final byte[] doubleDash = "--".getBytes();
        final byte[] lineBreakBytes = "\r\n".getBytes();

        final String header = "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" + "Content-Type: application/octet-stream";

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

        HttpRequest request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(stream.toByteArray())).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() / 100 != 2)
                throw new RuntimeException("Server replied with " + response);

            return response.body();
        });
    }
}

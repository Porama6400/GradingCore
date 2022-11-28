package dev.porama.gradingcore.common.file;

import dev.porama.gradingcore.common.seaweed.SeaweedConnector;
import dev.porama.gradingcore.common.seaweed.SeaweedException;
import lombok.Getter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class FileService {
    @Getter
    private final SeaweedConnector seaweedConnector;
    private final ExecutorService executorService;

    public FileService(ExecutorService executorService, SeaweedConnector seaweedConnector) {
        this.executorService = executorService;
        this.seaweedConnector = seaweedConnector;
    }

    public CompletableFuture<byte[]> read(FileSource file) {
        switch (file.getSourceType()) {
            case STRING -> {
                return CompletableFuture.supplyAsync(() -> file.getPayload().getBytes(StandardCharsets.UTF_8), executorService);
            }
            case BASE64 -> {
                return CompletableFuture.supplyAsync(() -> Base64.getDecoder().decode(file.getPayload()), executorService);
            }
            case SEAWEED -> {
//                return seaweedConnector.downloadFile();
            }
            case URL -> {
                return seaweedConnector.getFileUrl(URI.create(file.getPayload()));
            }
        }

        throw new SeaweedException("Failed to read file");
    }
}

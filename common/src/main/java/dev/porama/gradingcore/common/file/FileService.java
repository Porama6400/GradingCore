package dev.porama.gradingcore.common.file;

import dev.porama.gradingcore.common.seaweed.SeaweedConnector;
import dev.porama.gradingcore.common.seaweed.SeaweedException;
import lombok.Getter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

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
                return CompletableFuture.supplyAsync(() -> file.getSource().getBytes(StandardCharsets.UTF_8), executorService);
            }
            case BASE64 -> {
                return CompletableFuture.supplyAsync(() -> Base64.getDecoder().decode(file.getSource()), executorService);
            }
            case SEAWEED -> {
                return seaweedConnector.downloadFile(file.getSource());
            }
            case URL -> {
                return seaweedConnector.getFileUrl(URI.create(file.getSource()));
            }
        }

        throw new SeaweedException("Failed to read file");
    }

    public CompletableFuture<Map<String, byte[]>> readAll(List<FileSource> sourceList) {
        CompletableFuture<Map<String, byte[]>> future = new CompletableFuture<>();
        final Map<String, byte[]> files = new HashMap<>();

        AtomicInteger counter = new AtomicInteger(sourceList.size());
        sourceList.forEach(source -> {
            read(source).thenAccept(data -> {
                synchronized (files) {
                    files.put(source.getName(), data);
                }

                int pending = counter.decrementAndGet();
                if (pending == 0) {
                    future.complete(files);
                }
            });
        });

        return future;
    }
}

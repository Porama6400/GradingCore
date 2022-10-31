package dev.porama.gradingcore.container;

import dev.porama.gradingcore.container.data.ExecutionResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Container {
    String getContainerId();

    CompletableFuture<Void> start();

    CompletableFuture<Void> stop();


    CompletableFuture<ExecutionResult> execute();
    CompletableFuture<ExecutionResult> executeInside(String command);

    CompletableFuture<ExecutionResult> executeRaw(String command);

    CompletableFuture<Void> addFiles(Map<String, byte[]> files);

    CompletableFuture<Void> addFile(String path, byte[] data);

    CompletableFuture<byte[]> getFile(String path);
}

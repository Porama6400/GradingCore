package dev.porama.gradingcore.core.container;

import dev.porama.gradingcore.core.container.data.ExecutionResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Container {
    String getContainerId();

    CompletableFuture<Void> start();

    CompletableFuture<Void> attach();

    CompletableFuture<Void> kill();

    CompletableFuture<ExecutionResult> executeInside(String command);

    CompletableFuture<Void> sendInput(String input);

    CompletableFuture<Void> addFiles(Map<String, byte[]> files);

    CompletableFuture<Void> addFile(String path, byte[] data);

    CompletableFuture<byte[]> getFile(String path);
}

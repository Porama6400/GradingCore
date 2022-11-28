package dev.porama.gradingcore.core.container;

import dev.porama.gradingcore.core.container.data.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Container {
    String getContainerId();

    CompletableFuture<Void> start();

    CompletableFuture<Void> attach();

    CompletableFuture<Void> kill();

    CompletableFuture<ExecutionResult> executeInside(String command);

    CompletableFuture<Void> sendInput(String input);

    @Nullable String readOutputNow() throws IOException;

    CompletableFuture<@Nullable String> readOutput(int timeout);

    CompletableFuture<Void> addFiles(Map<String, byte[]> files);

    CompletableFuture<Void> addFile(String path, byte[] data);

    CompletableFuture<byte[]> getFile(String path);
}

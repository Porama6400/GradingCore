package dev.porama.gradingcore.core.container;

import dev.porama.gradingcore.core.container.data.ExecutionResult;
import dev.porama.gradingcore.core.temp.TempFileService;
import dev.porama.gradingcore.core.temp.TemporaryFile;
import dev.porama.gradingcore.core.utils.StreamUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class DockerContainer implements Container {
    private static final Logger logger = LoggerFactory.getLogger(DockerContainer.class);
    @Getter
    private final ContainerTemplate template;
    @Getter
    private final ExecutorService executorService;
    private final TempFileService tempFileService;
    @Getter
    @Nullable
    private String containerId = null;
    private Process monitorProcess;

    public DockerContainer(ContainerTemplate template, ExecutorService executorService, TempFileService tempFileService) {
        this.template = template;
        this.executorService = executorService;
        this.tempFileService = tempFileService;
    }

    public static void logProcessOutput(Process process) throws IOException {
        logger.debug("Process {} has output: {}, error: {}",
                process.pid(),
                StreamUtils.toString(process.getInputStream()),
                StreamUtils.toString(process.getErrorStream())
        );
    }

    public String applyCommandPlaceholders(String command) {
        command = command.replace("%image%", getTemplate().getImageId());
        command = command.replace("%time_limit_hard%", String.valueOf(getTemplate().getTimeLimitHard()));
//        command = command.replace("%name%", sessionId);
        command = command.replace("%workdir%", template.getWorkingDirectory().replace(" ", "\\ "));

        if (getContainerId() != null) {
            command = command.replace("%id%", this.getContainerId());
        }

        return command;
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executorService.execute(() -> {
            try {
                String effectiveStartCommand = applyCommandPlaceholders(template.getCommand());
                logger.debug("Starting a container with the command: {}", effectiveStartCommand);
                Process process = Runtime.getRuntime().exec(effectiveStartCommand);
                process.waitFor();
                containerId = StreamUtils.toString(process.getInputStream()).replaceAll("[^a-z0-9]", "");

                if (containerId.isEmpty()) {
                    throw new IOException("Failed to start: " + StreamUtils.toString(process.getErrorStream()));
                }

                logger.debug("Started container with id " + this.getContainerId());
                future.complete(null);
            } catch (IOException | InterruptedException ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Void> attach() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                monitorProcess = Runtime.getRuntime().exec("docker attach " + getContainerId());
                future.complete(null);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> kill() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executorService.execute(() -> {
            try {
                Process process = Runtime.getRuntime().exec("docker kill " + this.getContainerId());
                process.waitFor();
                future.complete(null);
            } catch (IOException | InterruptedException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<ExecutionResult> executeInside(String command) {
        return executeRaw("docker exec " + this.getContainerId() + " " + command);
    }

    @Override
    public CompletableFuture<Void> sendInput(String input) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                monitorProcess.getOutputStream().write(input.getBytes());
                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    @Nullable
    public String readOutputNow() throws IOException {
        InputStream inputStream = monitorProcess.getInputStream();
        if (inputStream.available() == 0) return null;
        return new String(inputStream.readNBytes(inputStream.available()));
    }

    @Override
    public CompletableFuture<@Nullable String> readOutput(int timeout) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executorService.submit(() -> {
            long timeoutThreshold = System.currentTimeMillis() + timeout;
            while (System.currentTimeMillis() < timeoutThreshold) {
                logger.debug("readOutputTick");
                try {
                    String output = readOutputNow();
                    logger.debug("Out: " + output);
                    if (output != null) {
                        future.complete(output);
                        return;
                    } else {
                        Thread.sleep(100);
                    }
                } catch (IOException | InterruptedException e) {
                    logger.debug("readOutputError", e);
                    future.completeExceptionally(e);
                    return;
                }
            }
            logger.debug("readOutputTimeout");
            future.completeExceptionally(new RuntimeException("Timeout while waiting for execution daemon stdout"));
        });
        return future;
    }

    public CompletableFuture<ExecutionResult> executeRaw(String command) {
        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();

        executorService.execute(() -> {
            try {
                long timeStart = System.currentTimeMillis();
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                long time = System.currentTimeMillis() - timeStart;

                ExecutionResult executionResult = new ExecutionResult(
                        StreamUtils.toString(process.getInputStream()),
                        StreamUtils.toString(process.getErrorStream()),
                        time
                );
                logger.debug(executionResult.toString());
                future.complete(executionResult);
            } catch (IOException | InterruptedException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Void> addFiles(Map<String, byte[]> files) {
        return CompletableFuture.supplyAsync(() -> {
            files.forEach((path, data) -> {
                addFile(path, data).join();
            });
            return null;
        }, executorService);
    }

    public CompletableFuture<Void> addFile(String path, byte[] data) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.execute(() -> {

            TemporaryFile temporaryFile = tempFileService.allocate();

            try (FileOutputStream stream = new FileOutputStream(temporaryFile.file())) {
                stream.write(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String effectiveCommand = "docker cp " + temporaryFile.file().getPath() + " " + this.getContainerId() + ":" + template.getWorkingDirectory() + path.replace(" ", "\\ ");
            executeRaw(effectiveCommand).thenAccept(result -> {
                logger.debug("Added file " + path + " to container " + this.getContainerId() + ":" + result);

                if (result.stderr().length() > 0) {
                    future.completeExceptionally(new RuntimeException("Execution error: " + effectiveCommand + " -> " + result.stderr()));
                }

                try {
                    tempFileService.free(temporaryFile);
                    future.complete(null);

                } catch (IOException e) {
                    future.completeExceptionally(new RuntimeException("Unable to delete temp file " + temporaryFile.file().getPath()));
                }

            }).exceptionally((ex) -> {
                tempFileService.tryFree(temporaryFile);
                future.completeExceptionally(ex);
                return null;
            });
        });
        return future;
    }

    @Override
    public CompletableFuture<byte[]> getFile(String path) {
        final CompletableFuture<byte[]> future = new CompletableFuture<>();
        executorService.execute(() -> {

            TemporaryFile temporaryFile = tempFileService.allocate();
            String effectiveCommand = "docker cp " +
                                      this.getContainerId() + ":" + template.getWorkingDirectory() + path.replace(" ", "\\ ") + " " +
                                      temporaryFile.file().getPath();

            executeRaw(effectiveCommand).thenAccept(result -> {
                try {
                    if (!temporaryFile.file().exists()) {
                        future.completeExceptionally(new FileNotFoundException("File not found: " + temporaryFile));
                        return;
                    }

                    if (result.stderr().length() > 0) {
                        future.completeExceptionally(new RuntimeException("Execution error: " + effectiveCommand + " -> " + result.stderr()));
                        return;
                    }

                    try (FileInputStream inputStream = new FileInputStream(temporaryFile.file())) {
                        byte[] bytes = StreamUtils.toBytes(inputStream);
                        future.complete(bytes);
                    }
                } catch (IOException ex) {
                    future.completeExceptionally(ex);
                } finally {
                    try {
                        tempFileService.free(temporaryFile);
                    } catch (IOException e) {
                        logger.error("Failed freeing temporary file " + temporaryFile.toString(), e);
                    }
                }

            }).exceptionally((ex) -> {
                tempFileService.tryFree(temporaryFile);
                future.completeExceptionally(ex);
                return null;
            });
        });
        return future;
    }

}

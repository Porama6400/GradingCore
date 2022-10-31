package dev.porama.gradingcore.grader;

import dev.porama.gradingcore.config.TemplateConfiguration;
import dev.porama.gradingcore.container.BasicContainer;
import dev.porama.gradingcore.container.Container;
import dev.porama.gradingcore.container.ContainerTemplate;
import dev.porama.gradingcore.container.data.ExecutionResult;
import dev.porama.gradingcore.temp.TempFileService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class GradingSession {
    @Getter
    private final Container container;
    @Getter
    private final Map<String, byte[]> files;
    @Getter
    private final CompletableFuture<ExecutionResult> resultFuture = new CompletableFuture<>();
    private final TempFileService tempFileService;
    @Getter
    private State state = State.STARTING;
    @Getter
    private ExecutionResult executionResult;

    private Logger logger = LoggerFactory.getLogger(GradingSession.class);

    public GradingSession(ContainerTemplate config, Map<String, byte[]> files, ExecutorService workerExecutor, TempFileService tempFileService) {
        this.files = files;
        this.tempFileService = tempFileService;
        this.container = new BasicContainer(config, workerExecutor, this.tempFileService);
    }

    public void setState(State state) {
        logger.debug("Container " + container.getContainerId() + " is changing state to " + state);
        this.state = state;
    }

    public void tick() {
        switch (state) {
            case STARTING -> {
                container.start()
                        .thenAccept(res -> setState(State.ADDING_FILES))
                        .exceptionally(ex -> {
                            logger.warn("Failed to start container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(ex);
                            setState(State.FINISHED);
                            return null;
                        });
            }
            case ADDING_FILES -> {
                container.addFiles(files).thenAccept((res) -> {
                    setState(State.EXECUTING);
                }).exceptionally(ex -> {
                    logger.warn("Failed to add files to " + container.getContainerId(), ex);
                    resultFuture.completeExceptionally(ex);
                    setState(State.STOPPING);
                    return null;
                });
            }
            case EXECUTING -> {
                container.execute().thenAccept((executionResult -> {
                    logger.debug("" + executionResult);
                    this.executionResult = executionResult;
                    resultFuture.complete(executionResult);
                    setState(State.STOPPING);
                })).exceptionally(ex -> {
                    logger.warn("Failed to execute in container " + container.getContainerId(), ex);
                    resultFuture.completeExceptionally(ex);
                    setState(State.STOPPING);
                    return null;
                });
            }
            case STOPPING -> {
                container.stop().thenAccept(res -> {
                    logger.debug("" + executionResult);
                    setState(State.FINISHED);
                }).exceptionally(ex -> {
                    logger.warn("Failed to stop container " + container.getContainerId(), ex);
                    resultFuture.completeExceptionally(ex);
                    setState(State.FINISHED);
                    return null;
                });
            }
            case FINISHED -> {
                if (executionResult == null) {
                    resultFuture.completeExceptionally(new IllegalStateException("Execution completed without a result"));
                }
            }
        }
    }

    public boolean isFinished() {
        return state == State.FINISHED;
    }

    public enum State {
        STARTING,
        ADDING_FILES,
        EXECUTING,
        STOPPING,
        FINISHED
    }
}

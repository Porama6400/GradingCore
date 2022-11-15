package dev.porama.gradingcore.core.grader;

import dev.porama.gradingcore.core.container.DockerContainer;
import dev.porama.gradingcore.core.container.Container;
import dev.porama.gradingcore.core.container.ContainerTemplate;
import dev.porama.gradingcore.core.container.data.ExecutionResult;
import dev.porama.gradingcore.core.temp.TempFileService;
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
        this.container = new DockerContainer(config, workerExecutor, this.tempFileService);
    }

    public void setState(State state) {
        logger.debug("Container " + container.getContainerId() + " is changing state to " + state);
        this.state = state;
    }

    public void tick() {
        switch (state) {
            case STARTING -> {
                container.start()
                        .thenAccept(res -> setState(State.ATTACH))
                        .exceptionally(ex -> {
                            logger.warn("Failed to start container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(ex);
                            setState(State.FINISHED);
                            return null;
                        });
            }
            case ATTACH -> {
                container.attach()
                        .thenAccept(res -> setState(State.ADDING_FILES))
                        .exceptionally(ex -> {
                            logger.warn("Failed to attach to container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(ex);
                            setState(State.FINISHED);
                            return null;
                        });
            }
            case ADDING_FILES -> {
                container.addFiles(files).thenAccept((res) -> {
                    container.sendInput("\n");
                    setState(State.EXECUTING);
                }).exceptionally(ex -> {
                    logger.warn("Failed to add files to " + container.getContainerId(), ex);
                    resultFuture.completeExceptionally(ex);

                    container.kill().exceptionally(killException -> {
                        logger.error("Failed to kill container " + container.getContainerId(), killException);
                        return null;
                    });

                    setState(State.FINISHED);
                    return null;
                });
            }
            case EXECUTING -> {

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
        ATTACH,
        ADDING_FILES,
        SIGNAL_READY,
        EXECUTING,
        FINISHED
    }
}

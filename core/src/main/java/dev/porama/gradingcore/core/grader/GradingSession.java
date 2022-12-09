package dev.porama.gradingcore.core.grader;

import dev.porama.gradingcore.common.file.FileService;
import dev.porama.gradingcore.common.file.FileSource;
import dev.porama.gradingcore.core.container.Container;
import dev.porama.gradingcore.core.container.ContainerTemplate;
import dev.porama.gradingcore.core.container.DockerContainer;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.temp.TempFileService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class GradingSession {
    @Getter
    private final Container container;
    private final FileService fileService;
    @Getter
    private final GradingRequest gradingRequest;
    @Getter
    private final CompletableFuture<GradingResult> resultFuture = new CompletableFuture<>();
    @Getter
    private final TempFileService tempFileService;
    @Getter
    private final ContainerTemplate template;
    @Getter
    private final GradingResult gradingResult = new GradingResult();
    @Getter
    private final Logger logger = LoggerFactory.getLogger(GradingSession.class);
    @Getter
    private State state = State.STARTING;
    @Getter
    private long stateStartTime = System.currentTimeMillis();

    public GradingSession(ContainerTemplate template, GradingRequest gradingRequest, ExecutorService workerExecutor, TempFileService tempFileService, FileService fileService) {
        this.gradingRequest = gradingRequest;
        this.tempFileService = tempFileService;
        this.template = template;
        this.container = new DockerContainer(this.template, workerExecutor, this.tempFileService);
        this.fileService = fileService;
    }

    public void setState(State state) {
        logger.debug("Container " + container.getContainerId() + " is changing state to " + state);
        this.stateStartTime = System.currentTimeMillis();
        this.state = state;
    }

    public long getStateTime() {
        return System.currentTimeMillis() - this.stateStartTime;
    }

    public void tick() {

        if (getStateTime() > 10000) {
            logger.error("Container state timeout: " + container.getContainerId() + " at " + state);
            setState(State.FINISHED);
            container.kill();
            resultFuture.completeExceptionally(new RuntimeException("State timeout"));
            return;
        }

        switch (state) {
            case STARTING -> {
                setState(State.STARTING_WAIT);
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
                setState(State.ATTACH_WAIT);
                container.attach()
                        .thenAccept(res -> setState(State.ADDING_FILES))
                        .exceptionally(ex -> {
                            logger.warn("Failed to attach to container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(ex);
                            setState(State.FINISHED);
                            return null;
                        });
            }
            case LOADING_FILES -> {

                setState(State.LOADING_FILES_WAIT);
            }
            case ADDING_FILES -> {
                setState(State.ADDING_FILES_WAIT);
                List<FileSource> fileSources = gradingRequest.getFilesSource();
                AtomicInteger fileCounter = new AtomicInteger(fileSources.size());

                fileSources.forEach(source -> {
                    fileService.read(source).thenAccept(data -> {
                        container.addFile(source.getName(), data).thenAccept(ignored -> {
                            logger.debug("Added file " + source.getName() + " to " + container.getContainerId());
                            fileCounter.decrementAndGet();
                            if (fileCounter.get() == 0) {
                                setState(State.EXECUTING);
                            }
                        }).exceptionally(ex -> {
                            logger.error("Failed reading file " + source.getName() + " for " + container.getContainerId(), ex);
                            fileCounter.decrementAndGet();
                            if (fileCounter.get() == 0) {
                                setState(State.EXECUTING);
                            }
                            return null;
                        });
                    }).exceptionally(ex -> {
                        logger.error("Failed reading file " + source.getName() + " for " + container.getContainerId(), ex);
                        fileCounter.decrementAndGet();
                        if (fileCounter.get() == 0) {
                            setState(State.EXECUTING);
                        }
                        return null;
                    });
                });
            }
            case EXECUTING -> {
                setState(State.EXECUTING_WAIT);
                container.addFile("start.lock", new byte[0]);
            }
            case EXECUTING_WAIT -> {
                container.getFile("executed.lock").thenAccept(result -> {
                    logger.debug("completed execution");
                    setState(State.SAVING);
                }).exceptionally(ex -> {
                    logger.debug("still waiting: ", ex);
                    return null;
                });
            }
            case SAVING -> {
                setState(State.SAVING_WAIT);
                for (String filePath : template.getOutputFiles()) {
                    container.getFile(filePath).thenAccept(result -> {
                        gradingResult.getFiles().put(filePath, result);
                        logger.debug("File " + filePath + ":" + new String(result));

                        if (gradingResult.getFiles().size() == template.getOutputFiles().size()) {
                            setState(State.FINISHED);
                        }

                    }).exceptionally(error -> {
                        gradingResult.getFiles().put(filePath, null);

                        if (gradingResult.getFiles().size() == template.getOutputFiles().size()) {
                            setState(State.FINISHED);
                        }
                        return null;
                    });
                }
            }
            case SAVING_WAIT -> {
                if (getStateTime() > 3000) {
                    logger.error("Timeout saving files");
                    setState(State.FINISHED);
                }
            }
            case FINISHED -> {
                container.addFile("done.lock", new byte[0]);
                resultFuture.complete(gradingResult);
            }
        }
    }

    public boolean isFinished() {
        return state == State.FINISHED;
    }

    public enum State {
        STARTING,
        STARTING_WAIT,
        ATTACH,
        ATTACH_WAIT,
        LOADING_FILES,
        LOADING_FILES_WAIT,
        ADDING_FILES,
        ADDING_FILES_WAIT,
        EXECUTING,
        EXECUTING_WAIT,
        SAVING,
        SAVING_WAIT,
        FINISHED
    }
}

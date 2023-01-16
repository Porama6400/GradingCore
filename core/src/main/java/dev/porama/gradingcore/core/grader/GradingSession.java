package dev.porama.gradingcore.core.grader;

import dev.porama.gradingcore.common.file.FileService;
import dev.porama.gradingcore.common.file.FileSource;
import dev.porama.gradingcore.core.container.Container;
import dev.porama.gradingcore.core.container.ContainerTemplate;
import dev.porama.gradingcore.core.container.DockerContainer;
import dev.porama.gradingcore.core.exception.GradingException;
import dev.porama.gradingcore.core.exception.GradingStateTimeoutException;
import dev.porama.gradingcore.core.exception.GradingTimeoutException;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.grader.data.GradingResultParser;
import dev.porama.gradingcore.core.grader.data.GradingStatus;
import dev.porama.gradingcore.core.temp.TempFileService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Logger logger = LoggerFactory.getLogger(GradingSession.class);
    private final Map<String, byte[]> fileMap = new ConcurrentHashMap<>();
    private final AtomicInteger savingCounter = new AtomicInteger();
    @Getter
    private State state = State.STARTING;
    @Getter
    private long stateStartTime = System.currentTimeMillis();
    @Getter
    private long startTime = System.currentTimeMillis();

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

    public long getRunningTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    public long getStateTime() {
        return System.currentTimeMillis() - this.stateStartTime;
    }

    public void tick() {

        if (getRunningTime() > template.getTimeLimitHard()) {
            logger.debug("Container " + container.getContainerId() + " timed out");
            setState(State.FINISHED);
            container.kill();
            resultFuture.completeExceptionally(new GradingTimeoutException("Container timeout"));
            return;
        }

        if (getStateTime() > template.getTimeLimitState()) {
            logger.error("Container state timeout: " + container.getContainerId() + " at " + state);
            if (getState() == State.EXECUTING_WAIT) {
                resultFuture.complete(new GradingResult(gradingRequest.getId(), GradingStatus.TIMEOUT_EXECUTION));
            } else {
                resultFuture.completeExceptionally(new GradingStateTimeoutException("State timeout at state " + getState()));
            }

            setState(State.FINISHED);
            container.kill();
            return;
        }

        switch (state) {
            case STARTING -> {
                setState(State.STARTING_WAIT);
                this.startTime = System.currentTimeMillis();
                container.start()
                        .thenAccept(res -> setState(State.ATTACH))
                        .exceptionally(ex -> {
                            logger.warn("Failed to start container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(new GradingException(true, ex));
                            setState(State.FINISHING);
                            return null;
                        });
            }
            case ATTACH -> {
                setState(State.ATTACH_WAIT);
                logger.info("Submission " + gradingRequest.getId() + " is using container " + container.getContainerId());
                container.attach()
                        .thenAccept(res -> setState(State.ADDING_FILES))
                        .exceptionally(ex -> {
                            logger.warn("Failed to attach to container " + container.getContainerId(), ex);
                            resultFuture.completeExceptionally(new GradingException(true, ex));
                            setState(State.FINISHING);
                            return null;
                        });
            }
            case ADDING_FILES -> {
                setState(State.ADDING_FILES_WAIT);
                List<FileSource> fileSources = gradingRequest.getFiles();
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
                    setState(State.SAVING);
                }).exceptionally(ex -> {
                    logger.debug(container.getContainerId() + "still waiting");
                    return null;
                });
            }
            case SAVING -> {
                setState(State.SAVING_WAIT);
                savingCounter.set(template.getOutputFiles().size());
                for (String filePath : template.getOutputFiles()) {
                    container.getFile(filePath).handle((result, error) -> {
                        if (error instanceof CompletionException completionException) {
                            error = completionException.getCause();
                        }
                        try {
                            int waitingAmount = savingCounter.decrementAndGet();

                            logger.debug("File " + filePath + ":" + (result == null ? "null" : new String(result)) + " waiting for " + waitingAmount);

                            if (result != null) {
                                fileMap.put(filePath, result);
                            } else if (error instanceof FileNotFoundException) {
                                logger.info("File not found while saving: " + filePath);
                            } else if (error != null) {
                                logger.error("Failed saving file " + filePath, error);
                            }

                            if (waitingAmount == 0) {
                                setState(State.FINISHING);
                            }
                        } catch (Exception ex) {
                            logger.warn("Failed saving file {}", filePath);
                            ex.printStackTrace();
                        }
                        return null;
                    });
                }
            }
            case SAVING_WAIT -> {
                if (getStateTime() > 5000) {
                    logger.error("Timeout saving files");
                    setState(State.FINISHING);
                }
            }
            case FINISHING -> {
                container.addFile("done.lock", new byte[0]).handle((res, ex) -> {
                    setState(State.FINISHED);
                    return null;
                });


                GradingResult parse = GradingResultParser.parse(gradingRequest.getId(), fileMap);
                parse.setDuration(System.currentTimeMillis() - startTime);
                resultFuture.complete(parse);
                setState(State.FINISHING_WAIT);
            }
            case FINISHED -> {
                container.kill().thenAccept(res -> {
                    logger.debug("Container " + container.getContainerId() + " is killed");
                }).exceptionally(ignored -> {
                    logger.debug("Container " + container.getContainerId() + " is shutdown gracefully");
                    return null;
                });
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
        ADDING_FILES,
        ADDING_FILES_WAIT,
        EXECUTING,
        EXECUTING_WAIT,
        SAVING,
        SAVING_WAIT,
        FINISHING,
        FINISHING_WAIT,
        FINISHED
    }
}

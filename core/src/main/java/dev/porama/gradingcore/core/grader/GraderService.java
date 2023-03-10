package dev.porama.gradingcore.core.grader;

import dev.porama.gradingcore.common.file.FileService;
import dev.porama.gradingcore.common.seaweed.SeaweedConnector;
import dev.porama.gradingcore.core.config.MainConfiguration;
import dev.porama.gradingcore.core.config.TemplateService;
import dev.porama.gradingcore.core.container.ContainerTemplate;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.temp.TempFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GraderService {

    private final TemplateService templateService;
    private final List<GradingSession> sessionList = new ArrayList<>();
    private final ExecutorService gradingServiceThreadPool = Executors.newCachedThreadPool();
    private final TempFileService tempFileService = new TempFileService(new File("temp"));
    private final SeaweedConnector seaweedConnector = new SeaweedConnector(gradingServiceThreadPool);
    private final FileService fileService = new FileService(gradingServiceThreadPool, seaweedConnector);

    private final Logger logger = LoggerFactory.getLogger(GraderService.class);

    public GraderService(TemplateService templateService, ScheduledExecutorService masterThreadPool, MainConfiguration config) {
        this.templateService = templateService;

        masterThreadPool.scheduleAtFixedRate(this::tick, config.getTickInterval(), config.getTickInterval(), TimeUnit.MILLISECONDS);
    }

    public void tick() {
        synchronized (sessionList) {
            sessionList.forEach(entry -> {
                try {
                    entry.tick();
                } catch (Exception ex) {
                    logger.error("Unhandled error ticking grading for submission " + entry.getGradingRequest().getId(), ex);
                }
            });
            sessionList.removeIf(GradingSession::isFinished);
        }
    }


    public CompletableFuture<GradingResult> submit(GradingRequest request) {
        ContainerTemplate config = templateService.get(request.getType());

        if (config == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid template " + request.getType()));
        }

        GradingSession gradingSession = new GradingSession(config, request, gradingServiceThreadPool, tempFileService, fileService);
        return submit(gradingSession);
    }

    public CompletableFuture<GradingResult> submit(GradingSession session) {
        synchronized (sessionList) {
            sessionList.add(session);
        }

        return session.getResultFuture();
    }

    public void shutdown() {
        logger.info("Shutting down");
        for (GradingSession gradingSession : sessionList) {
            gradingSession.getContainer().kill();
            gradingSession.setState(GradingSession.State.FINISHED);
        }
        sessionList.clear();

        gradingServiceThreadPool.shutdown();
        tempFileService.shutdown();
    }
}

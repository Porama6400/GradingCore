package dev.porama.gradingcore.grader;

import dev.porama.gradingcore.config.TemplateService;
import dev.porama.gradingcore.container.data.ContainerTemplate;
import dev.porama.gradingcore.messenger.Messenger;
import dev.porama.gradingcore.grader.data.GradingRequest;
import dev.porama.gradingcore.grader.data.GradingResult;
import dev.porama.gradingcore.temp.TempFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GraderService {

    private final TemplateService templateService;
    private final Messenger messenger;
    private final List<GradingSession> sessionList = new ArrayList<>();
    private ExecutorService executingThreadPool = Executors.newCachedThreadPool();
    private ScheduledExecutorService schedulingThreadPool = Executors.newScheduledThreadPool(1);
    private TempFileService tempFileService = new TempFileService(new File("temp"));

    private Logger logger = LoggerFactory.getLogger(GraderService.class);

    public GraderService(TemplateService templateService, Messenger messenger) {
        this.templateService = templateService;
        this.messenger = messenger;

        schedulingThreadPool.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    public void tick() {
        synchronized (sessionList) {
            sessionList.forEach(entry -> {
                try {
                    entry.tick();
                } catch (Exception ex) {
                    logger.error("Failed ticking session " + entry.getContainer().getContainerId(), ex);
                }
            });
            sessionList.removeIf(GradingSession::isFinished);
        }
    }


    public CompletableFuture<GradingResult> submit(GradingRequest request) {
        ContainerTemplate template = templateService.get(request.getType());
        Objects.requireNonNull(template);

        GradingSession gradingSession = new GradingSession(template, request.getFiles(), executingThreadPool, tempFileService);
        return submit(gradingSession);
    }

    public CompletableFuture<GradingResult> submit(GradingSession session) {
        synchronized (sessionList) {
            sessionList.add(session);
        }

        return session.getResultFuture().thenApply((executionResult -> {
            return new GradingResult();
            //TODO do a real grading
        }));
    }

    public void handle(GradingRequest message) {

    }

    public void shutdown() {
        while (sessionList.size() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        schedulingThreadPool.shutdown();
        executingThreadPool.shutdown();
    }
}

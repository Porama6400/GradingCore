package dev.porama.gradingcore.core.scheduler;

import dev.porama.gradingcore.core.config.MainConfiguration;
import dev.porama.gradingcore.core.grader.GraderService;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.grader.data.GradingStatus;
import dev.porama.gradingcore.core.messenger.RequeueLimiter;
import dev.porama.gradingcore.core.postprocessor.PostProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Scheduler {

    private final GraderService graderService;
    private final PostProcessorService postProcessor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MainConfiguration config;
    private final RequeueLimiter requeueLimiter = new RequeueLimiter();
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private long nextTimeSlot = 0;


    public Scheduler(GraderService graderService, ScheduledExecutorService scheduledExecutorService, PostProcessorService postProcessor, MainConfiguration config) {
        this.graderService = graderService;
        this.postProcessor = postProcessor;
        this.scheduledExecutorService = scheduledExecutorService;
        this.config = config;

        this.scheduledExecutorService.scheduleAtFixedRate(requeueLimiter::resetAll, 15, 15, TimeUnit.MINUTES);
    }

    public void handle(RequestFuturePair requestFuturePair) {
        GradingRequest request = requestFuturePair.getRequest();
        CompletableFuture<GradingResult> future = requestFuturePair.getFuture();

        requeueLimiter.increment(request.getId());
        if (requeueLimiter.hasExceeded(request.getId(), config.getMaxRequeue())) {
            logger.warn("Submission {} has exceeded the maximum requeue limit", request.getId());
            future.complete(new GradingResult(
                    request,
                    GradingStatus.REQUEUE_LIMIT_EXCEEDED
            ));
        }

        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            if (nextTimeSlot < currentTime) {
                nextTimeSlot = currentTime;
            }
            long delay = nextTimeSlot - currentTime;
            nextTimeSlot += config.getTimeSlotWidth();

            logger.info("Scheduled " + request.getId() + " to run " + delay + "ms in the future");
            delayingFuture(() -> graderService.submit(request), delay)
                    .thenApply((res) -> {
                        logger.info("Post processing" + request.getId());
                        postProcessor.apply(res);
                        return res;
                    })
                    .thenAccept(future::complete)
                    .exceptionally(err -> {
                        future.completeExceptionally(err);
                        return null;
                    });
        }
    }

    public <T> CompletableFuture<T> delayingFuture(Supplier<CompletableFuture<T>> supplier, long delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        scheduledExecutorService.schedule(() -> {
            CompletableFuture<T> realFuture = supplier.get();
            realFuture.thenAccept(future::complete).exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        }, delay, TimeUnit.MILLISECONDS);
        return future;
    }
}

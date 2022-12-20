package dev.porama.gradingcore.core.messenger;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Messenger {
    void listen(Function<GradingRequest, CompletableFuture<GradingResult>> requestConsumer) throws IOException;

    void publishResult(GradingResult result) throws IOException;

    void shutdown();
}

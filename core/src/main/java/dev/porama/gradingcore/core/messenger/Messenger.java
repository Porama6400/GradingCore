package dev.porama.gradingcore.core.messenger;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.scheduler.RequestFuturePair;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Messenger {

    void listen(ExecutorService executor, Consumer<RequestFuturePair> requestConsumer) throws IOException;

    void publishResult(GradingResult result) throws IOException;

    void shutdown();
}

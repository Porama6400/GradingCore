package dev.porama.gradingcore.core.scheduler;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
public class RequestFuturePair {
    private GradingRequest request;
    private CompletableFuture<GradingResult> future;
}

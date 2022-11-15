package dev.porama.gradingcore.core.container.data;

public record ExecutionResult(String stdout, String stderr, long executionTime) {
}

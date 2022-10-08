package dev.porama.gradingcore.container.data;

public record ExecutionResult(String stdout, String stderr, long executionTime) {
}

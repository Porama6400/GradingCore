package dev.porama.gradingcore.core.grader.data;

public enum GradingStatus {
    COMPLETED,
    FAILED_COMPILATION,
    FAILED_MISSING_RESULT,
    FAILED_MISSING_TEST,
    TIMEOUT_EXECUTION,
    TIMEOUT_CONTAINER,
    REQUEUE_LIMIT_EXCEEDED
}

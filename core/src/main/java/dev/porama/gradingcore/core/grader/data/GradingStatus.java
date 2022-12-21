package dev.porama.gradingcore.core.grader.data;

public enum GradingStatus {
    COMPLETED,

    FAILED_COMPILATION,
    FAILED_EXECUTION,
    FAILED_GENERIC,
    FAILED_MISSING_RESULT,

    TIMEOUT_EXECUTION,
    TIMEOUT_CONTAINER,
    TIMEOUT_GENERIC,

    REQUEUE_LIMIT_EXCEEDED
}

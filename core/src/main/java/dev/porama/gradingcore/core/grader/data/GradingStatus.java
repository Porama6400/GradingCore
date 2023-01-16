package dev.porama.gradingcore.core.grader.data;

public enum GradingStatus {
    PASSED,
    FAILED_RESULT,
    FAILED_COMPILATION,
    FAILED_CONTAINER,
    FAILED_MEMORY_LIMIT,
    MISSING_RESULT,
    MISSING_TEST,
    TIMEOUT_EXECUTION,
    TIMEOUT_CONTAINER,
    REQUEUE_LIMIT_EXCEEDED
}

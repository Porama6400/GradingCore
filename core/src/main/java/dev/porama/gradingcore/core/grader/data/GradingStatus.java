package dev.porama.gradingcore.core.grader.data;

public enum GradingStatus {
    PASSED,
    FAILED_RESULT,
    FAILED_COMPILATION,
    FAILED_MISSING_RESULT,
    FAILED_MISSING_TEST,
    FAILED_CONTAINER,
    TIMEOUT_EXECUTION,
    TIMEOUT_CONTAINER,
    REQUEUE_LIMIT_EXCEEDED
}

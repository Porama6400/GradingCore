package dev.porama.gradingcore.core.grader.data;

import lombok.Data;

@Data
public class GradingResult {

    private int submissionId;
    private ResultType status;
    private String result;

    public GradingResult() {

    }

    public GradingResult(int submissionId, ResultType status, String result) {
        this.submissionId = submissionId;
        this.status = status;
        this.result = result;
    }

    public enum ResultType {
        COMPLETED,
        ERROR_GENERIC,
        TIMEOUT,
        REQUEUE_LIMIT_EXCEEDED, COMPILATION_FAILED
    }
}

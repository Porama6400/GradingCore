package dev.porama.gradingcore.core.grader.data;

import lombok.Data;

@Data
public class GradingResult {
    private int submissionId;
    private ResultType status;
    private String result;

    public enum ResultType {
        COMPLETED,
        ERROR_GENERIC,
        TIMEOUT,
        COMPILATION_FAILED
    }
}

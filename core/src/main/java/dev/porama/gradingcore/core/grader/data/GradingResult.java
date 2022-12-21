package dev.porama.gradingcore.core.grader.data;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class GradingResult {

    private int submissionId;
    private GradingStatus status;
    private @Nullable String result;

    public GradingResult() {

    }

    public GradingResult(int submissionId, GradingStatus status, @Nullable String result) {
        this.submissionId = submissionId;
        this.status = status;
        this.result = result;
    }
}

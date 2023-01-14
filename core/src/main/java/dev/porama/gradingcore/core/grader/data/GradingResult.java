package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.core.utils.ParserUtils;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
public class GradingResult {

    private int id;
    private GradingStatus status;
    private @Nullable String result;
    private @Nullable String compilationLog;
    private long duration = -1;

    public GradingResult() {

    }

    public GradingResult(int id, GradingStatus status) {
        this(id, status, null, null);
    }

    public GradingResult(int id, GradingStatus status, @Nullable String result, @Nullable String compilationLog) {
        this.id = id;
        this.status = status;
        this.result = result;
        this.compilationLog = compilationLog;
    }

    public static GradingResult parse(int submissionId, Map<String, byte[]> fileMap) {
        String result = ParserUtils.parseFileMap(fileMap, "result.txt");
        String compilationLog = ParserUtils.parseFileMap(fileMap, "compilationLog.txt");
        String statusText = ParserUtils.parseFileMap(fileMap, "status.txt");

        GradingStatus status;
        try {
            if (statusText == null || statusText.isEmpty()) {
                status = GradingStatus.COMPLETED;
            } else {
                statusText = statusText.split("[\\r\\n]")[0];
                status = GradingStatus.valueOf(statusText);
            }
        } catch (IllegalArgumentException ignored) {
            status = GradingStatus.COMPLETED;
        }

        if (status == GradingStatus.COMPLETED && result == null) {
            status = GradingStatus.FAILED_MISSING_RESULT;
        }

        return new GradingResult(submissionId, status, result, compilationLog);

    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}

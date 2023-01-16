package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.serialize.SerializeIgnore;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
public class GradingResult {

    private int id;
    private GradingStatus status;
    @Nullable
    private String compilationLog;
    @Nullable
    @SerializeIgnore
    private Map<String, byte[]> files;
    private long duration = -1;

    public GradingResult(int id, GradingStatus status) {
        this(id, status, null, null);;
    }
    public GradingResult(int id, GradingStatus status, @Nullable String compilationLog, @Nullable Map<String, byte[]> files) {
        this.id = id;
        this.status = status;
        this.compilationLog = compilationLog;
        this.files = files;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}

package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.serialize.SerializeIgnore;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Data
public class GradingResult {

    private int id;
    private GradingStatus status;
    @Nullable
    private String compilationLog;
    private Map<String, Object> metadata;


    @Nullable
    @SerializeIgnore
    private Map<String, byte[]> files;
    @SerializeIgnore
    private GradingRequest request;

    public GradingResult(GradingRequest request, GradingStatus status) {
        this(request, status, null, null);
    }

    public GradingResult(GradingRequest request, GradingStatus status, @Nullable String compilationLog, @Nullable Map<String, byte[]> files) {
        this.request = request;
        this.id = request.getId();
        this.status = status;
        this.compilationLog = compilationLog;
        this.files = files;
        this.metadata = request.getMetadata() == null ? new HashMap<>() : new HashMap<>(request.getMetadata());
    }
}

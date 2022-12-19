package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.file.FileSource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GradingRequest {
    private String type;
    private String submissionId;
    private long softLimitMemory;
    private long softLimitTime;
    private List<FileSource> filesSource = new ArrayList<>();

    public GradingRequest(String type, Map<String, byte[]> files) {
        this.type = type;
        files.forEach((key, value) -> filesSource.add(FileSource.base64(key, value)));
    }

    public List<FileSource> getFilesSource() {
        return filesSource;
    }
}

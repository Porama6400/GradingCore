package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.file.FileSource;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GradingRequest {
    private int id;
    private String type;

    /**
     * optionally contains:
     * long softLimitMemory
     * long softLimitTime
     */
    private Map<String, String> metadata = new HashMap<>();
    private List<FileSource> filesSource = new ArrayList<>();

    public GradingRequest(String type, Map<String, byte[]> files) {
        this.type = type;
        files.forEach((key, value) -> filesSource.add(FileSource.base64(key, value)));
    }

    public List<FileSource> getFilesSource() {
        return filesSource;
    }
}

package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.file.FileSource;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GradingRequest {
    public String type;
    public List<FileSource> fileSources = new ArrayList<>();

    public GradingRequest(String type, Map<String, byte[]> files) {
        this.type = type;
        files.forEach((key, value) -> fileSources.add(FileSource.base64(key, value)));
    }

    public Map<String, byte[]> getFiles() {
        Map<String, byte[]> files = new HashMap<>();
        fileSources.forEach(entry -> files.put(entry.getName(), entry.getData()));
        return files;
    }
}

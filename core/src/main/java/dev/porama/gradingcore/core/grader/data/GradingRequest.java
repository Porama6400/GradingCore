package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.common.file.FileService;
import dev.porama.gradingcore.common.file.FileSource;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Data
public class GradingRequest {
    public String type;
    public List<FileSource> filesSource = new ArrayList<>();

    public GradingRequest(String type, Map<String, byte[]> files) {
        this.type = type;
        files.forEach((key, value) -> filesSource.add(FileSource.base64(key, value)));
    }

    public List<FileSource> getFilesSource() {
        return filesSource;
    }
}

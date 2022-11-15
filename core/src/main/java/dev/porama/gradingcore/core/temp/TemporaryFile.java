package dev.porama.gradingcore.core.temp;

import java.io.File;

public record TemporaryFile(File file, boolean isDirectory, long allocationTime) {
    String getPath() {
        return file.getPath();
    }
}
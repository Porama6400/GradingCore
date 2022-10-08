package dev.porama.gradingcore.temp;

import java.io.File;

public record TemporaryFile(File file, boolean isDirectory, long allocationTime) {
    String getPath() {
        return file.getPath();
    }
}
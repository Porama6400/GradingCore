package dev.porama.gradingcore.core.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ParserUtils {
    public static @Nullable String parseFileMap(Map<String, byte[]> fileMap, String fileName) {
        byte[] bytes = fileMap.get(fileName);
        if (bytes == null) return null;
        return new String(bytes);
    }
}

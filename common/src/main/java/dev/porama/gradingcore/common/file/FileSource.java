package dev.porama.gradingcore.common.file;

import java.util.Base64;

public class FileSource {
    private final String name;
    private final FileSourceType sourceType;
    private final String payload;

    public FileSource(String name, FileSourceType sourceType, String payload) {
        this.name = name;
        this.sourceType = sourceType;
        this.payload = payload;
    }

    public static FileSource base64(String name, byte[] data) {
        return new FileSource(name, FileSourceType.BASE64, Base64.getEncoder().encodeToString(data));
    }

    public static FileSource string(String name, String data) {
        return new FileSource(name, FileSourceType.BASE64, data);
    }

    public String getName() {
        return name;
    }

    public FileSourceType getSourceType() {
        return sourceType;
    }

    public String getPayload() {
        return payload;
    }
}

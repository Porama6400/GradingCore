package dev.porama.gradingcore.core.utils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Objects;

public class ConfigUtils {


    public static void assertExists(File file) {
        assertExists(file, "/" + file.getName());
    }

    public static void assertExists(File file, String resourcePath) {
        if (file.exists()) return;

        InputStream inputStream = ConfigUtils.class.getResourceAsStream(resourcePath);
        Objects.requireNonNull(inputStream);

        try (FileOutputStream outputStream = new FileOutputStream(file);) {
            StreamUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseFile(File file, Type type) throws IOException {
        try (FileReader fileReader = new FileReader(file)) {
            return SerializerUtils.getGson().fromJson(fileReader, type);
        }
    }

    public static <T> T load(File file, Type type) throws IOException {
        assertExists(file);
        return parseFile(file, type);
    }
}

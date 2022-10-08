package dev.porama.gradingcore.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Objects;

public class ConfigUtils {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(SerializeIgnore.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }).create();

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
            return GSON.fromJson(fileReader, type);
        }
    }

    public static <T> T load(File file, Type type) throws IOException {
        assertExists(file);
        return parseFile(file, type);
    }

    public static <T> T fromJson(String body, Class<T> type) {
        return fromJson(body.getBytes(), type);
    }

    public static <T> T fromJson(byte[] body, Class<T> type) {
        return GSON.fromJson(new String(body), type);
    }

    public static <T> String toJson(T data) {
        return GSON.toJson(data);
    }
}

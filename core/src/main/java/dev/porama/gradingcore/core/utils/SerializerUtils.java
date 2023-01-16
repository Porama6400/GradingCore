package dev.porama.gradingcore.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.porama.gradingcore.common.serialize.SerializeIgnoreStrategy;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SerializerUtils {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setExclusionStrategies(new SerializeIgnoreStrategy())
            .create();

    public static Gson getGson() {
        return GSON;
    }

    public static @Nullable String parseFileMap(Map<String, byte[]> fileMap, String fileName) {
        byte[] bytes = fileMap.get(fileName);
        if (bytes == null) return null;
        return new String(bytes);
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

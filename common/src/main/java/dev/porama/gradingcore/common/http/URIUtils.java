package dev.porama.gradingcore.common.http;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class URIUtils {

    public static URI construct(String base, String path, @Nullable Map<String, String> parameters) throws URISyntaxException {
        if (!base.endsWith("/")) {
            base += "/";
        }

        return new URI(base + path + serializeParameters(parameters));
    }

    public static String serializeParameters(@Nullable Map<String, String> map) {
        if (map == null || map.size() == 0) return "";

        StringBuilder builder = new StringBuilder("?");

        map.forEach((key, value) -> {
            builder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        });

        return builder.toString();
    }
}


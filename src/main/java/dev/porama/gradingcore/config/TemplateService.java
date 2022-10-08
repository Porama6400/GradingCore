package dev.porama.gradingcore.config;

import com.google.gson.reflect.TypeToken;
import dev.porama.gradingcore.container.data.ContainerTemplate;
import dev.porama.gradingcore.utils.ConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateService {

    private final Type containerTemplateArrayType = new TypeToken<List<ContainerTemplate>>() {
    }.getType();

    private final Map<String, ContainerTemplate> templateMap = new HashMap<>();

    public TemplateService() throws IOException {
        List<ContainerTemplate> templates = ConfigUtils.load(new File("images.json"), containerTemplateArrayType);

        for (ContainerTemplate template : templates) {
            templateMap.put(template.getId(), template);
        }
    }

    @Nullable
    public ContainerTemplate get(String id) {
        return templateMap.get(id);
    }
}

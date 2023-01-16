package dev.porama.gradingcore.core.config;

import dev.porama.gradingcore.core.container.ContainerTemplate;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import dev.porama.gradingcore.core.utils.SerializerUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateService {
    private final TemplateConfiguration configuration;
    private final Map<String, ContainerTemplate> templateMap = new HashMap<>();
    private final Logger logger;

    public TemplateService() throws IOException {
        configuration = ConfigUtils.load(new File("images.json"), TemplateConfiguration.class);
        logger = LoggerFactory.getLogger(TemplateService.class);

        for (ContainerTemplate template : configuration.getTemplates()) {
            if (template.getId() == null ||
                template.getImageId() == null ||
                template.getCommand() == null ||
                template.getWorkingDirectory() == null ||
                template.getTimeLimitHard() == 0
            ) {
                logger.error("Container template " + template.getId() + " is invalid, skipping, " + SerializerUtils.toJson(template));
            } else {
                logger.info("Registering container template " + template.getId());
                templateMap.put(template.getId(), template);
            }
        }
    }

    @Nullable
    public ContainerTemplate get(String id) {
        return templateMap.get(id);
    }
}

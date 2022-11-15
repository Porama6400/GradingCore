package dev.porama.gradingcore.core.config;

import dev.porama.gradingcore.core.container.ContainerTemplate;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public final class TemplateConfiguration {
    @Getter
    List<ContainerTemplate> templates = new ArrayList<>();
}

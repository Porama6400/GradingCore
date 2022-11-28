package dev.porama.gradingcore.core.container;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContainerTemplate {
    private String id;
    private String imageId;
    private String command;
    private long timeLimitHard;
    private String workingDirectory;
    private List<String> outputFiles;

    public ContainerTemplate(String id, String imageId, String command, long timeLimitHard, String workingDirectory, List<String> outputFiles) {
        this.id = id;
        this.imageId = imageId;
        this.command = command;
        this.timeLimitHard = timeLimitHard;
        this.workingDirectory = workingDirectory;
        this.outputFiles = outputFiles;

        if (!this.workingDirectory.endsWith("/")) this.workingDirectory += "/";
    }
}

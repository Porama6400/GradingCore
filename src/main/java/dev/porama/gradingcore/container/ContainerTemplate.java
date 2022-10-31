package dev.porama.gradingcore.container;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerTemplate {
    private String id;
    private String imageId;
    private String command;
    private long timeLimitHard;
    private String workingDirectory;

    public ContainerTemplate(String id, String imageId, String command, long timeLimitHard, String workingDirectory){
        this.id = id;
        this.imageId = imageId;
        this.command = command;
        this.timeLimitHard = timeLimitHard;
        this.workingDirectory = workingDirectory;
    }
}

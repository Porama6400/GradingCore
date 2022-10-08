package dev.porama.gradingcore.container.data;

import lombok.Setter;

import java.util.Objects;

@Setter
public final class ContainerTemplate {
    private String id;
    private String imageId;
    private String startCommand;
    private String runCommand;

    private long timeLimitHard;
    private String workingDirectory;

    public ContainerTemplate(String id, String imageId, String startCommand, String runCommand, long timeLimitHard, String workingDirectory) {
        this.id = id;
        this.imageId = imageId;
        this.startCommand = startCommand;
        this.runCommand = runCommand;
        this.timeLimitHard = timeLimitHard;
        this.workingDirectory = workingDirectory;
    }

    public String getId() {
        Objects.requireNonNull(id);
        return id;
    }

    public String getImageId() {
        Objects.requireNonNull(imageId);
        return imageId;
    }

    public String getStartCommand() {
        Objects.requireNonNull(startCommand);
        return startCommand;
    }

    public String getRunCommand() {
        Objects.requireNonNull(runCommand);
        return runCommand;
    }

    public String getWorkingDirectory() {
        Objects.requireNonNull(workingDirectory);
        if (workingDirectory.endsWith("/"))
            workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
        return workingDirectory;
    }

    public long getTimeLimitHard() {
        if (timeLimitHard == 0) {
            throw new RuntimeException("Time hard limit is not set");
        }
        return this.timeLimitHard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerTemplate template = (ContainerTemplate) o;
        return timeLimitHard == template.timeLimitHard && Objects.equals(id, template.id) && Objects.equals(imageId, template.imageId) && Objects.equals(startCommand, template.startCommand) && Objects.equals(runCommand, template.runCommand) && Objects.equals(workingDirectory, template.workingDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageId, startCommand, runCommand, timeLimitHard, workingDirectory);
    }
}

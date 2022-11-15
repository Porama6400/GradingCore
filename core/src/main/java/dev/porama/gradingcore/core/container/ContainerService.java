package dev.porama.gradingcore.core.container;

import dev.porama.gradingcore.core.temp.TempFileService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContainerService {
    private final ExecutorService executor;
    private final TempFileService tempFileService;

    public ContainerService(TempFileService tempFileService) {
        this.executor = Executors.newCachedThreadPool();
        this.tempFileService = tempFileService;
    }

    public Container create(ContainerTemplate template) {
        return new DockerContainer(template, executor, tempFileService);
    }
}

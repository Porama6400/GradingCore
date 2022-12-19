package dev.porama.gradingcore.core.temp;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class TempFileService {
    private final File tempRoot;
    private final AtomicLong fileIndex = new AtomicLong(0);
    private final Set<TemporaryFile> fileSet = new HashSet<>();
    private final Set<TemporaryFile> directorySet = new HashSet<>();

    public TempFileService(File tempDirectory) {
        this.tempRoot = tempDirectory;
        tempRoot.mkdirs();
        if (!tempRoot.isDirectory()) throw new RuntimeException("Failed to create temp directory");
    }

    public TemporaryFile allocate() {
        File file = new File(tempRoot, String.valueOf(fileIndex.getAndIncrement()));
        TemporaryFile temporaryFile = new TemporaryFile(file, false, System.currentTimeMillis());
        fileSet.add(temporaryFile);
        return temporaryFile;
    }

    public synchronized void free(TemporaryFile file) throws IOException {
        if (!fileSet.contains(file)) throw new IllegalArgumentException("Tried to free non-existing temporary file");

        if (!file.file().delete()) throw new IOException("Unable to delete file " + file.file().getAbsolutePath());
        fileSet.remove(file);
    }

    public boolean tryFree(TemporaryFile file) {
        try {
            free(file);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public synchronized TemporaryFile allocateDirectory() throws IOException {
        File file = new File(tempRoot, String.valueOf(fileIndex.getAndIncrement()));
        if (!file.mkdirs()) throw new IOException("Unable to create directory " + file.getAbsolutePath());
        TemporaryFile temporaryDirectory = new TemporaryFile(file, true, System.currentTimeMillis());

        directorySet.add(temporaryDirectory);
        return temporaryDirectory;
    }

    public synchronized void freeDirectory(TemporaryFile directory) throws IOException {
        if (!directorySet.contains(directory))

        deleteDirectory(directory.file());
        directorySet.remove(directory);
    }

    private void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        if (!f.delete()) throw new IOException("Unable to delete file " + f.getAbsolutePath());
                    }
                }
            }
        }

        if (!file.delete()) throw new IOException("Unable to delete file " + file.getAbsolutePath());
    }

    public void shutdown() {

    }
}

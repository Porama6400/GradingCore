package dev.porama.gradingcore.common.zip;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static Map<String, byte[]> readSome(File zipFile, Set<String> fileNames) throws IOException {
        Map<String, byte[]> dataMap = new HashMap<>();
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (String fileName : fileNames) {
                ZipEntry entry = zip.getEntry(fileName);
                if (entry == null) {
                    continue;
                }
                try (BufferedInputStream zipInputStream = new BufferedInputStream(zip.getInputStream(entry))) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = zipInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, count);
                    }
                    dataMap.put(fileName, byteArrayOutputStream.toByteArray());
                }
            }
        }
        return dataMap;
    }

    public static Map<String, byte[]> readAll(File zipFile) throws IOException {
        final Map<String, byte[]> dataMap = new HashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                dataMap.put(entry.getName(), zipInputStream.readAllBytes());
            }
        }
        return dataMap;
    }
}

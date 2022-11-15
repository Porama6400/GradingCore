package dev.porama.gradingcore.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    private StreamUtils() {

    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int readLength;

        while (inputStream.available() > 0) {
            readLength = inputStream.read(buffer, 0, buffer.length);
            outputStream.write(buffer, 0, readLength);
        }
    }

    public static byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copy(inputStream, outputStream);
        return outputStream.toByteArray();
    }

    public static String toString(InputStream inputStream) throws IOException {
        return new String(toBytes(inputStream));
    }
}

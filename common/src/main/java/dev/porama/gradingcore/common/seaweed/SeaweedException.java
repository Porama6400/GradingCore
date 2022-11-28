package dev.porama.gradingcore.common.seaweed;

public class SeaweedException extends RuntimeException {
    public SeaweedException(String message) {
        super(message);
    }

    public SeaweedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

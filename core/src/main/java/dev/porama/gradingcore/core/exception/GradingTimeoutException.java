package dev.porama.gradingcore.core.exception;

public class GradingTimeoutException extends GradingException {
    public GradingTimeoutException(String message) {
        super(true, message);
    }
}

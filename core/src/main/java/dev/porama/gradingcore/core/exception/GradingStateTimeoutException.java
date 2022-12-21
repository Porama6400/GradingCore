package dev.porama.gradingcore.core.exception;

public class GradingStateTimeoutException extends GradingException {
    public GradingStateTimeoutException(String message) {
        super(true, message);
    }
}

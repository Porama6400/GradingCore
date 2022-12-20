package dev.porama.gradingcore.core.exception;

import java.util.concurrent.TimeoutException;

public class GradingTimeoutException extends TimeoutException {
    public GradingTimeoutException(String message) {
        super(message);
    }

    public GradingTimeoutException() {
        super();
    }
}

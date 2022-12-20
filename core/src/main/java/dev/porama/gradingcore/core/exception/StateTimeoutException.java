package dev.porama.gradingcore.core.exception;

import java.util.concurrent.TimeoutException;

public class StateTimeoutException extends TimeoutException {
    public StateTimeoutException(String message) {
        super(message);
    }

    public StateTimeoutException() {
        super();
    }
}

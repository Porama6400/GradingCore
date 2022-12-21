package dev.porama.gradingcore.core.exception;

import org.jetbrains.annotations.Nullable;

public class GradingException extends Exception {
    private final boolean retryAllowed;
    private final @Nullable String message;
    private final @Nullable Throwable cause;

    public GradingException(boolean retryAllowed, String message) {
        this(retryAllowed, message, null);
    }

    public GradingException(boolean retryAllowed, Throwable throwable) {
        this(retryAllowed, null, throwable);
    }

    public GradingException(boolean retryAllowed, @Nullable String message, @Nullable Throwable cause) {
        this.retryAllowed = retryAllowed;
        this.message = message;
        this.cause = cause;
    }

    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Nullable
    public Throwable getCause() {
        return cause;
    }
}

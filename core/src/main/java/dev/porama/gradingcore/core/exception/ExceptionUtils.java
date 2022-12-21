package dev.porama.gradingcore.core.exception;

import java.util.concurrent.CompletionException;

public class ExceptionUtils {
    public static Throwable unwrapCompletionException(Throwable throwable) {
        if (throwable == null) return null;

        while (throwable instanceof CompletionException exception) {
            throwable = exception.getCause();
        }
        return throwable;
    }

    public static boolean isRetryAllowed(Throwable throwable) {
        if (throwable instanceof GradingException exception) {
            return exception.isRetryAllowed();
        }

        return true;
    }
}

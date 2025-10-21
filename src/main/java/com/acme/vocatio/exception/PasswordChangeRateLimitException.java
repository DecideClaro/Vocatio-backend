package com.acme.vocatio.exception;

/** Indica que se alcanzó el límite de intentos para cambiar la contraseña. */
public class PasswordChangeRateLimitException extends RuntimeException {
    private final long retryAfterSeconds;

    public PasswordChangeRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}

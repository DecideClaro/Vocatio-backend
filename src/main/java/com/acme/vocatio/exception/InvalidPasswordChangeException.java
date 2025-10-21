package com.acme.vocatio.exception;

/** Se lanza cuando la nueva contraseña no cumple la política establecida. */
public class InvalidPasswordChangeException extends RuntimeException {
    public InvalidPasswordChangeException(String message) {
        super(message);
    }
}

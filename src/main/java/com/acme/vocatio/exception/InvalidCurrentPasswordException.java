package com.acme.vocatio.exception;

/** Se lanza cuando la contraseña actual no coincide con la registrada. */
public class InvalidCurrentPasswordException extends RuntimeException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}

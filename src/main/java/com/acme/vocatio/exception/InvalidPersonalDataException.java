package com.acme.vocatio.exception;

/** Se lanza cuando los datos personales no cumplen los criterios permitidos. */
public class InvalidPersonalDataException extends RuntimeException {
    public InvalidPersonalDataException(String message) {
        super(message);
    }
}

package com.acme.vocatio.exception;

/** Se lanza cuando la confirmación de borrado no coincide con el texto esperado. */
public class InvalidAccountDeletionConfirmationException extends RuntimeException {

    public InvalidAccountDeletionConfirmationException(String message) {
        super(message);
    }
}

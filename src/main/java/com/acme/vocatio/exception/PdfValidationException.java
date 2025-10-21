package com.acme.vocatio.exception;

/**
 * Excepción lanzada cuando la validación de un archivo PDF falla.
 */
public class PdfValidationException extends RuntimeException {
    
    public PdfValidationException(String message) {
        super(message);
    }
    
    public PdfValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
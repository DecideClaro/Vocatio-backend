package com.acme.vocatio.exception;

/**
 * Excepción lanzada cuando una URL proporcionada no es válida.
 */
public class InvalidUrlException extends RuntimeException {
    
    public InvalidUrlException(String message) {
        super(message);
    }
    
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
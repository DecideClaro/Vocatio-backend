package com.acme.vocatio.exception;

/**
 * Excepción lanzada cuando un archivo no puede ser descargado.
 */
public class FileNotDownloadableException extends RuntimeException {
    
    public FileNotDownloadableException(String message) {
        super(message);
    }
    
    public FileNotDownloadableException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.acme.vocatio.exception;

/** Excepción para indicar que una carrera no existe. */
public class CareerNotFoundException extends RuntimeException {

    public CareerNotFoundException(Long careerId) {
        super("Carrera no encontrada");
    }
}

package com.ejercicioIntegrador.dulcemayor.exceptions;

/**
 * Excepcion de negocio reutilizada del Integrador 1.
 * La lanzan los servicios cuando una regla de negocio o una validacion no se cumple.
 */
public class MiException extends Exception {

    public MiException(String mensaje) {
        super(mensaje);
    }
}

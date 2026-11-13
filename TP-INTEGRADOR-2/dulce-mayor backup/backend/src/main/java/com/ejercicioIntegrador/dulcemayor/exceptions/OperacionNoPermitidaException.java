package com.ejercicioIntegrador.dulcemayor.exceptions;

/**
 * Se lanza cuando se intenta ejecutar un caso de uso que una entidad no admite
 * (por ejemplo, dar de baja un registro de stock).
 */
public class OperacionNoPermitidaException extends MiException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}

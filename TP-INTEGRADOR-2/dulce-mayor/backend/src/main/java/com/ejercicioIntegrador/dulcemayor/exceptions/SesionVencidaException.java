package com.ejercicioIntegrador.dulcemayor.exceptions;

/** La sesión no se puede renovar: superó la duración máxima desde el ingreso. Se responde 401. */
public class SesionVencidaException extends MiException {

    public SesionVencidaException(String mensaje) {
        super(mensaje);
    }
}

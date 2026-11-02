package com.ejercicioIntegrador.dulcemayor.exceptions;

/**
 * Se lanza cuando se consulta por identificador un recurso que no existe o esta dado de baja.
 * Extiende MiException para que los servicios sigan declarando una unica excepcion de negocio.
 */
public class RecursoNoEncontradoException extends MiException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

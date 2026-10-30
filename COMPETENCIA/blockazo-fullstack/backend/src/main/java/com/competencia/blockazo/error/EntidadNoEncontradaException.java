package com.competencia.blockazo.error;

/** La entidad buscada no existe o esta dada de baja. Se responde con 404. */
public class EntidadNoEncontradaException extends ErrorServiceException {

    public EntidadNoEncontradaException(String msg) {
        super(msg);
    }
}

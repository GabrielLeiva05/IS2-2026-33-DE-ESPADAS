package com.competencia.blockazo.error;

/** Error de negocio (patronTemplate). Se responde con 400 Bad Request. */
public class ErrorServiceException extends Exception {

    public ErrorServiceException() {}

    public ErrorServiceException(String msg) {
        super(msg);
    }
}

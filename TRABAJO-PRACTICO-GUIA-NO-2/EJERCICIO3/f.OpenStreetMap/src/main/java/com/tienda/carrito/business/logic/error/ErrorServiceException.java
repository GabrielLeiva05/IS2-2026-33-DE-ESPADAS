package com.tienda.carrito.business.logic.error;

/**
 * Excepcion CHECKED (igual que en patronTemplate) que lanza la capa de servicio cuando se viola una
 * regla de negocio. El mensaje es apto para mostrarse al usuario.
 */
public class ErrorServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public ErrorServiceException(String mensaje) {
        super(mensaje);
    }
}

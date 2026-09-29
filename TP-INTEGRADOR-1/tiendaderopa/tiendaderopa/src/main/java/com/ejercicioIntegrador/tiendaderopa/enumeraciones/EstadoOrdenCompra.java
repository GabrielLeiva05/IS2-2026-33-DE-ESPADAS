package com.ejercicioIntegrador.tiendaderopa.enumeraciones;

public enum EstadoOrdenCompra {
    PENDIENTE_DE_PAGO,
    PAGO_REALIZADO,
    PENDIENTE_DE_ENTREGA,
    PENDIENTE_DE_ENVIO,
    ENTREGADO,
    ANULADA,

    // Se conservan para leer órdenes persistidas con el esquema anterior.
    PENDIENTE_COMPLETAR,
    PENDIENTE_PAGO,
    PENDIENTE_ENVIO,
    PENDIENTE_ENTREGA
}
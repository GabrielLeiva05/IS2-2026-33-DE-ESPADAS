package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Medio de pago preferido por el cliente, elegido por el preventista al confirmar el pedido en forma presencial.
 * EFECTIVO: se abona al recibir el pedido.
 * TRANSFERENCIA: se envía por correo el detalle, el total y el alias para transferir.
 * QR: se envía por correo el detalle, el total y el QR de Mercado Pago (Checkout API).
 */
public enum FormaDePago {
    EFECTIVO,
    TRANSFERENCIA,
    QR
}

package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Estado del pago de un pedido.
 * PENDIENTE: el pago todavía no se acreditó (efectivo al entregar, transferencia o QR sin confirmar).
 * APROBADO: Mercado Pago confirmó el pago del QR; la factura del pedido pasa a PAGADA.
 * RECHAZADO: Mercado Pago informó el rechazo del pago.
 */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO
}

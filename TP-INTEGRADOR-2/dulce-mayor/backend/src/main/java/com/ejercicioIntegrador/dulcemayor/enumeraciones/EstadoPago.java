package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Estado del pago, independiente del estado del pedido (plan Fase 3, ítem 8).
 * Solo PENDIENTE permite acreditar; ACREDITADO es terminal.
 */
public enum EstadoPago {
    PENDIENTE,
    ACREDITADO,
    RECHAZADO,
    EXPIRADO,
    CANCELADO
}

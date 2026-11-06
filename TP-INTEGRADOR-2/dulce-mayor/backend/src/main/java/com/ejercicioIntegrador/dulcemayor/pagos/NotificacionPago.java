package com.ejercicioIntegrador.dulcemayor.pagos;

/**
 * Resultado de procesar una notificación del proveedor. Si el pago externo todavía no está
 * aprobado, {@code acreditado} queda en false y el pago interno no cambia de estado.
 */
public record NotificacionPago(String referenciaOperacion, boolean acreditado, String referenciaExterna) {
}

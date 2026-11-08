package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Estados de la factura (diagrama, bloque Facturación y pagos).
 * En esta etapa toda factura nace PENDIENTE_PAGO. PAGADA la asignará el módulo de Pagos al registrar un pago real
 * (HU-28) y ANULADA la anulación de facturas; ninguna de las dos transiciones está implementada todavía.
 */
public enum EstadoFactura {
    PENDIENTE_PAGO,
    PAGADA,
    ANULADA
}

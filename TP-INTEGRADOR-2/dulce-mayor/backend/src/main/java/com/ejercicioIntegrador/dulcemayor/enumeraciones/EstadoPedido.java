package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Estados del pedido según el diagrama:
 *  CARGADO              el preventista lo cargó; el cliente todavía no lo confirmó.
 *  PENDIENTE_DE_ENTREGA el cliente lo confirmó (se cobra al entregar mientras no exista el módulo de pagos).
 *  ENTREGADO            el distribuidor lo entregó.
 *  ANULADO              el cliente lo anuló antes de confirmarlo; su stock volvió al almacén.
 * "Confirmado" no es un estado: lo indica la fecha de confirmación (fechaFacturacion).
 */
public enum EstadoPedido {
    CARGADO,
    PENDIENTE_DE_ENTREGA,
    ENTREGADO,
    ANULADO
}

package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Confirmación del pago. En transferencia, referencia es el número de operación; en Mercado Pago,
 * el identificador del pago que se verifica server-to-server. En efectivo es opcional.
 */
public record ConfirmarPagoRequestDTO(String referencia, String observacion) {
}

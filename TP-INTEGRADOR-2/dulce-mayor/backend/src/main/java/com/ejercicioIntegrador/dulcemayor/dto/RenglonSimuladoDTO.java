package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Renglón de un pedido simulado: precio vigente, subtotal a precio de lista y descuento del renglón. */
public record RenglonSimuladoDTO(String idProducto, String nombreProducto, int cantidad, BigDecimal precioUnitario,
                                 BigDecimal subtotal, BigDecimal descuento) {
}

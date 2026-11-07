package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Vista previa del descuento que produciría un código sobre un subtotal. */
public record PrevisualizarPromocionRequestDTO(String codigo, BigDecimal subtotal) {
}

package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Resultado de aplicar un código promocional sobre el subtotal elegible. */
public record DescuentoPromocionDTO(String codigo, BigDecimal subtotal, BigDecimal descuento,
                                    BigDecimal total) {
}

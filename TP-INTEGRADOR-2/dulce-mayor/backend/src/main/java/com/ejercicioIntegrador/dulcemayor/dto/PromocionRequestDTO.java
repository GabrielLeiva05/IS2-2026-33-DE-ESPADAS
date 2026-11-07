package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Alta o modificación de una promoción. */
public record PromocionRequestDTO(String codigo, String descripcion, TipoPromocion tipo, BigDecimal valor,
                                  LocalDate fechaDesde, LocalDate fechaHasta, Boolean activa, Boolean usoUnico,
                                  BigDecimal minimoCompra) {
}

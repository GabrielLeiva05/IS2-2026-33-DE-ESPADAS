package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionResponseDTO(String id, String codigo, String descripcion, TipoPromocion tipo,
                                   BigDecimal valor, LocalDate fechaDesde, LocalDate fechaHasta, boolean activa,
                                   boolean usoUnico, BigDecimal minimoCompra, boolean vigente,
                                   boolean eliminado) implements BaseResponseDTO {
}

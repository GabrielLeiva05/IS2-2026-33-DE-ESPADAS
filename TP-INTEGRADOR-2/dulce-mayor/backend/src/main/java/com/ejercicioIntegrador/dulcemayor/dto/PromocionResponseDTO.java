package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Promoción para administración. "resumen" es una descripción legible del descuento ("15 %", "10 % desde 20 bultos"). */
public record PromocionResponseDTO(String id, TipoPromocion tipo, String nombre, String descripcion,
                                   LocalDate fechaInicio, LocalDate fechaFin, boolean activa, boolean vigente,
                                   List<ReferenciaDTO> productos, List<ReferenciaDTO> subCategorias,
                                   BigDecimal porcentaje, List<TramoVolumenDTO> tramos, String codigo,
                                   TipoValorDescuento tipoValor, BigDecimal valor, BigDecimal montoMinimo,
                                   Integer usosMaximos, int usosRealizados, boolean acumulable, String resumen,
                                   boolean eliminado) implements BaseResponseDTO {
}

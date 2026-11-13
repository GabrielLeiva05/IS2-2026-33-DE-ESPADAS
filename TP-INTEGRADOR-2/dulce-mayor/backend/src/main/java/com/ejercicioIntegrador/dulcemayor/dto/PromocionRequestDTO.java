package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Alta o modificación de una promoción. Según el tipo se usan:
 *  - PORCENTAJE_PRODUCTO: porcentaje + productos/subcategorías;
 *  - VOLUMEN: tramos + productos/subcategorías;
 *  - CODIGO: codigo, tipoValor, valor, montoMinimo, usosMaximos, acumulable (productos/subcategorías opcionales).
 */
public record PromocionRequestDTO(TipoPromocion tipo, String nombre, String descripcion, LocalDate fechaInicio,
                                  LocalDate fechaFin, Boolean activa, List<String> idsProductos,
                                  List<String> idsSubCategorias, BigDecimal porcentaje, List<TramoVolumenDTO> tramos,
                                  String codigo, TipoValorDescuento tipoValor, BigDecimal valor,
                                  BigDecimal montoMinimo, Integer usosMaximos, Boolean acumulable) {
}

package com.dulcemayor.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Cuerpo que se envía a POST/PUT /api/promociones. */
public record PromocionSolicitudDTO(String tipo, String nombre, String descripcion, LocalDate fechaInicio,
                                    LocalDate fechaFin, Boolean activa, List<String> idsProductos,
                                    List<String> idsSubCategorias, BigDecimal porcentaje, List<Tramo> tramos,
                                    String codigo, String tipoValor, BigDecimal valor, BigDecimal montoMinimo,
                                    Integer usosMaximos, Boolean acumulable) {

    public record Tramo(Integer cantidadDesde, Integer cantidadHasta, BigDecimal porcentaje) {
    }
}

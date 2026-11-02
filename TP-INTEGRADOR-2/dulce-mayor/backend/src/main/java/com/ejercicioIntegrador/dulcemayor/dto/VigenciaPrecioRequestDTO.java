package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDate;

/** Datos de entrada de una vigencia de precio. fechaHasta es opcional (vigencia abierta). */
public record VigenciaPrecioRequestDTO(LocalDate fechaDesde, LocalDate fechaHasta, Double precio,
                                       String idProducto) {
}

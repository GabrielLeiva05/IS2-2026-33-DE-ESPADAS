package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Tramo de una promoción por volumen: cantidadHasta null = "en adelante". */
public record TramoVolumenDTO(Integer cantidadDesde, Integer cantidadHasta, BigDecimal porcentaje) {
}

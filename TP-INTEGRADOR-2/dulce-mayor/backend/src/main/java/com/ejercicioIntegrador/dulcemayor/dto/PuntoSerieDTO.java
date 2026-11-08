package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Punto de una serie temporal (por ejemplo, pedidos por preventista por día o por mes). */
public record PuntoSerieDTO(String periodo, String serie, long cantidad, BigDecimal importe) {
}

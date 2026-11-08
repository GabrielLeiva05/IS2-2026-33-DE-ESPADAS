package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

/** Fila agregada de un reporte: agrupada por departamento, preventista, cliente o estado. */
public record FilaReporteDTO(String grupo, long pedidos, long presentaciones, BigDecimal importe) {
}

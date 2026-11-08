package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reporte de ventas del período. Incluye el total facturado, el desglose por la dimensión pedida y
 * el estado de los pedidos, para que la pantalla pueda mostrar totales y detalle sin otra consulta.
 */
public record ReporteVentasDTO(LocalDate desde, LocalDate hasta, String agrupacion,
                               long pedidos, long presentaciones, BigDecimal importeTotal,
                               List<FilaReporteDTO> filas, Map<String, Long> porEstado) {
}

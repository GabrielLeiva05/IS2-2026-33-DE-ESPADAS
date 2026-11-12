package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Histórico de ventas de un período, discriminado por departamento y preventista.
 * Origen único de la pantalla y del Excel. "departamentoFiltrado" y "preventistaFiltrado" son null si no se filtró.
 */
public record ReporteVentasDTO(LocalDate desde, LocalDate hasta, String departamentoFiltrado,
                               String preventistaFiltrado, LocalDateTime generadoEn, String generadoPor,
                               List<VentaDTO> ventas, List<SubtotalVentasDTO> porDepartamento,
                               List<SubtotalVentasDTO> porPreventista,
                               List<SubtotalVentasDTO> porDepartamentoYPreventista, long cantidadPedidos,
                               long cantidadClientes, double totalGeneral) {
}

package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;

import java.time.LocalDate;

/**
 * Filtros de los reportes administrativos. Todos son opcionales salvo lo que exija cada reporte
 * (el histórico de ventas obliga a indicar el período). El servicio los valida; el controlador solo los transporta.
 * Las fechas son inclusivas: "hasta" incluye todo el día indicado.
 * "estado" solo lo usa el reporte de clientes y pedidos; "idPreventista" solo el histórico de ventas.
 */
public record FiltroReporteDTO(LocalDate desde, LocalDate hasta, String idDepartamento, String idPreventista,
                               EstadoPedido estado) {
}

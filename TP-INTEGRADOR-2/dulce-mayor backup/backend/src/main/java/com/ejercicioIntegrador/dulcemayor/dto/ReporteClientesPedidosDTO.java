package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte general de clientes y pedidos. Es el único origen de datos de la pantalla, del PDF y del Excel:
 * los tres muestran exactamente lo que armó ServicioReporte con los mismos filtros.
 * Los filtros "desde", "hasta", "estadoFiltrado" y su descripción son null cuando no se aplicaron.
 * Los comercios sin pedidos en el período no aparecen: el reporte tiene una fila por pedido.
 */
public record ReporteClientesPedidosDTO(LocalDate desde, LocalDate hasta, String departamentoFiltrado,
                                        EstadoPedido estadoFiltrado, String descripcionEstadoFiltrado,
                                        LocalDateTime generadoEn, String generadoPor,
                                        List<PedidoReporteDTO> pedidos, long cantidadComercios,
                                        long cantidadPedidos, long cantidadEntregados,
                                        long cantidadPendientesDeEntrega, double totalFacturado) {
}

package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;

import java.time.LocalDateTime;

/**
 * Una fila del reporte de clientes y pedidos: un pedido con los datos de su cliente y de su comercio.
 * montoFacturado es null mientras el cliente no confirmó el pedido (o si fue anulado).
 * estadoEnvio resume el estado y la logística del pedido (ver ServicioReporte).
 */
public record PedidoReporteDTO(String idPuntoVenta, String idCliente, String numeroDocumentoCliente,
                               String nombreCliente, String correoCliente, String nombreComercio,
                               String departamento, String domicilioComercio, long numeroPedido,
                               LocalDateTime fechaHoraCarga, String preventista, EstadoPedido estado,
                               String descripcionEstado, String estadoEnvio, String distribuidor,
                               LocalDateTime fechaFacturacion, LocalDateTime fechaEntrega,
                               Double montoFacturado) {
}

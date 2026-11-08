package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDateTime;

/**
 * Una venta del histórico: un pedido confirmado por el cliente (facturado). fechaVenta es la fecha de
 * confirmación/facturación del pedido. Los importes son los del pedido (subtotal, descuento y total).
 */
public record VentaDTO(String idDepartamento, String departamento, String idPreventista, String preventista,
                       String idCliente, String numeroDocumentoCliente, String nombreCliente,
                       String nombreComercio, long numeroPedido, LocalDateTime fechaVenta, double subtotal,
                       double descuento, double total) {
}

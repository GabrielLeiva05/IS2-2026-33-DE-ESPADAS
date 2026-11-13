package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.model.Pedido;

/**
 * DTO INTERNO de ServicioFacturaCliente (crearFacturaCliente(idPedido) del diagrama). No lo recibe ningún
 * controlador: la factura de cliente la emite ServicioPedido al confirmar el pedido, dentro de la misma transacción,
 * con el pedido ya bloqueado. Por eso viaja la entidad y no su id.
 */
public record SolicitudFacturaClienteDTO(Pedido pedido) {
}

package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pedido completo. "confirmado" indica que el cliente ya lo confirmó (fechaConfirmacion = fechaFacturacion
 * del diagrama). Las acciones posibles se informan para que las vistas no repliquen reglas del negocio.
 * idFactura/numeroFactura/totalFacturado: la FacturaCliente que se emitió al confirmar (total con IVA, el que se
 * cobra al entregar); son null mientras el pedido no está confirmado.
 */
public record PedidoResponseDTO(String id, long numeroPedido, LocalDateTime fechaHoraCarga,
                                LocalDateTime fechaConfirmacion, LocalDateTime fechaDespacho,
                                LocalDateTime fechaEntrega, double subtotal, double descuento, double total,
                                EstadoPedido estado, boolean confirmado, boolean puedeConfirmarse,
                                boolean puedeAnularse, String idPuntoVenta, String nombreFantasia, String direccion,
                                String idCliente, String nombreCliente, String idPreventista,
                                String nombrePreventista, String idDistribuidor, String nombreDistribuidor,
                                List<DetallePedidoResponseDTO> detalles, boolean eliminado,
                                String idFactura, Long numeroFactura, Double totalFacturado,
                                FormaDePago formaDePago, EstadoPago estadoPago) implements BaseResponseDTO {
}

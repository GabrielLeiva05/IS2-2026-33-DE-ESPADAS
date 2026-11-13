package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/**
 * Carga (o simulación) de pedido desde la app del preventista. El preventista sale del token, no del cuerpo.
 * codigoDescuento es opcional; el descuento siempre lo calcula el backend (no se aceptan importes del celular).
 */
public record PedidoRequestDTO(String idPuntoVenta, List<LineaPedidoDTO> detalles, String codigoDescuento) {

    public PedidoRequestDTO(String idPuntoVenta, List<LineaPedidoDTO> detalles) {
        this(idPuntoVenta, detalles, null);
    }
}

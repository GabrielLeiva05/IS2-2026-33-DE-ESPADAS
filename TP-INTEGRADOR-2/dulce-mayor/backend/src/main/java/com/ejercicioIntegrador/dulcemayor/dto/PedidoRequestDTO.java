package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** Carga de pedido desde la app del preventista. El preventista sale del token, no del cuerpo. */
public record PedidoRequestDTO(String idPuntoVenta, List<LineaPedidoDTO> detalles) {
}

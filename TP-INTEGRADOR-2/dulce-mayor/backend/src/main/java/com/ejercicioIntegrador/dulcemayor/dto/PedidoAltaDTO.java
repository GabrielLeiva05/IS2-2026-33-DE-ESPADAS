package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** DTO interno del alta (Template Method): el pedido del preventista más el preventista autenticado. */
public record PedidoAltaDTO(String idPreventista, String idPuntoVenta, List<LineaPedidoDTO> detalles) {
}

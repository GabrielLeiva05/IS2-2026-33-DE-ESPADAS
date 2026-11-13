package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;

/** Pedido resumido para las paradas de entrega: número, bultos (unidades pedidas) y monto a cobrar. */
public record PedidoResumenDTO(String id, long numeroPedido, EstadoPedido estado, int cantidadBultos, double total) {
}

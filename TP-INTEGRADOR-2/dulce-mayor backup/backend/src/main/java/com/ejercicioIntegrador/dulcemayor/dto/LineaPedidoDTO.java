package com.ejercicioIntegrador.dulcemayor.dto;

/** Producto y cantidad elegidos por el preventista. El precio NO viaja: lo toma el backend del precio vigente. */
public record LineaPedidoDTO(String idProducto, Integer cantidad) {
}

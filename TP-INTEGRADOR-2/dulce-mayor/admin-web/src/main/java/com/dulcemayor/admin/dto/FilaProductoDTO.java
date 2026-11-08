package com.dulcemayor.admin.dto;

/** Fila del listado del ABM: el producto con su precio vigente (puede no tener) y su stock. */
public record FilaProductoDTO(ProductoDTO producto, Double precioVigente, StockDTO stock) {

    public boolean isSinPrecio() {
        return precioVigente == null;
    }
}

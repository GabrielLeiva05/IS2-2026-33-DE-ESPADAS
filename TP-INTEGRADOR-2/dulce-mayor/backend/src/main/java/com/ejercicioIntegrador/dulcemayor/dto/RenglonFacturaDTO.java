package com.ejercicioIntegrador.dulcemayor.dto;

/** Renglón de una factura de proveedor: producto, cantidad y precio unitario sin IVA. */
public record RenglonFacturaDTO(String idProducto, Integer cantidad, Double precioUnitario) {
}

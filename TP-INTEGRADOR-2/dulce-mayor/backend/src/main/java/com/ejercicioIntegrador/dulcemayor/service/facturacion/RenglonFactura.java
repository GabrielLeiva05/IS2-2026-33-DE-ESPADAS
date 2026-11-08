package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.model.Producto;

/** Renglón ya resuelto (producto, cantidad, precio unitario sin IVA) que un creador convierte en DetalleFactura. */
public record RenglonFactura(Producto producto, int cantidad, double precioUnitario) {
}

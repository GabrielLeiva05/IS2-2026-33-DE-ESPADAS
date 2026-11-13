package com.ejercicioIntegrador.dulcemayor.service.promociones;

import com.ejercicioIntegrador.dulcemayor.model.Producto;

import java.math.BigDecimal;

/** Renglón sobre el que se calculan descuentos: producto, cantidad y precio unitario vigente (sin IVA). */
public record RenglonDescuento(Producto producto, int cantidad, BigDecimal precioUnitario) {

    public BigDecimal subtotal() {
        return CalculadorDescuentos.dinero(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
    }
}

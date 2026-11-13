package com.ejercicioIntegrador.dulcemayor.service.promociones;

import com.ejercicioIntegrador.dulcemayor.model.CodigoDescuento;

import java.math.BigDecimal;
import java.util.List;

/** Resultado del cálculo: subtotal a precio de lista, descuentos aplicados y total (todo sin IVA). */
public record ResultadoDescuentos(BigDecimal subtotal, List<DescuentoCalculado> descuentos, BigDecimal totalDescuento,
                                  BigDecimal total, CodigoDescuento codigo) {

    public BigDecimal descuentoDelRenglon(int indice) {
        return descuentos.stream().filter(d -> d.indiceRenglon() == indice).map(DescuentoCalculado::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

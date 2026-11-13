package com.ejercicioIntegrador.dulcemayor.service.promociones;

import com.ejercicioIntegrador.dulcemayor.model.Promocion;

import java.math.BigDecimal;

/**
 * Un descuento resultante del cálculo.
 *
 * @param indiceRenglon posición del renglón (o -1 si es sobre el pedido completo)
 * @param porcentaje    porcentaje aplicado (null para un importe fijo)
 */
public record DescuentoCalculado(int indiceRenglon, Promocion promocion, BigDecimal porcentaje, BigDecimal importe) {

    public static final int PEDIDO_COMPLETO = -1;
}

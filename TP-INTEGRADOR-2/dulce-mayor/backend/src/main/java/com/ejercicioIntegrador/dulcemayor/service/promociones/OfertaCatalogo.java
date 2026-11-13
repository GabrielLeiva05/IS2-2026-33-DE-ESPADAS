package com.ejercicioIntegrador.dulcemayor.service.promociones;

import java.math.BigDecimal;
import java.util.List;

/**
 * Promociones automáticas de un producto para mostrarlas en el catálogo.
 *
 * @param porcentajeUnitario mejor porcentaje para una sola unidad (0 si solo hay descuentos por volumen)
 * @param textos             descripción de cada promoción ("15 % de descuento", "10 % desde 20 bultos")
 */
public record OfertaCatalogo(BigDecimal porcentajeUnitario, List<String> textos) {

    public static final OfertaCatalogo NINGUNA = new OfertaCatalogo(BigDecimal.ZERO, List.of());
}

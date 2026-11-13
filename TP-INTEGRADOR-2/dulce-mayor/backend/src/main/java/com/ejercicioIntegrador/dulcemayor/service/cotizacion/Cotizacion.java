package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Cotización del dólar tal como la informa la fuente externa.
 *
 * @param tipo               tipo pedido a la fuente (oficial, bolsa, blue, …)
 * @param nombre             nombre legible ("Oficial", "Bolsa", …)
 * @param compra             precio de compra en pesos
 * @param venta              precio de venta en pesos (es el que se usa para convertir)
 * @param fechaActualizacion cuándo actualizó la fuente esa cotización
 */
public record Cotizacion(String tipo, String nombre, BigDecimal compra, BigDecimal venta,
                         OffsetDateTime fechaActualizacion) {
}

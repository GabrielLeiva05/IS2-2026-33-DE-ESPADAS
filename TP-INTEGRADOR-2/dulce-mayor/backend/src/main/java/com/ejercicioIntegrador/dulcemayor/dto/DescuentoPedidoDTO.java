package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;

import java.math.BigDecimal;

/** Descuento aplicado a un pedido (o simulado). idProducto/nombreProducto null = sobre el pedido completo. */
public record DescuentoPedidoDTO(TipoPromocion tipo, String nombrePromocion, String codigo, BigDecimal porcentaje,
                                 BigDecimal importe, String idProducto, String nombreProducto) {
}

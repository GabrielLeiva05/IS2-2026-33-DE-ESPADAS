package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;

public record PedidoLineaResponseDTO(String idProducto, String nombreProducto, int cantidadPresentaciones,
                                     BigDecimal precioUnitario, BigDecimal subtotal) {
}

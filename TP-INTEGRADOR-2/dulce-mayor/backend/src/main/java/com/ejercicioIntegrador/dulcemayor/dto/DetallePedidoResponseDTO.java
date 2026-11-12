package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;

public record DetallePedidoResponseDTO(String id, String idProducto, String codigoProducto, String nombreProducto,
                                       Presentacion presentacion, int unidadesPorPresentacion, int cantidad,
                                       double precioUnitario, double subtotal, String idImagen) {
}

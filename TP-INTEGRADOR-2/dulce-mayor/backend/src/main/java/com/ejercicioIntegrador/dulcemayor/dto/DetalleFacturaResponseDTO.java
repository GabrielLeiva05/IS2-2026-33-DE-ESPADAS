package com.ejercicioIntegrador.dulcemayor.dto;

/** Renglón de factura para el exterior (precio unitario y subtotal sin IVA). */
public record DetalleFacturaResponseDTO(String id, String idProducto, String codigoProducto, String nombreProducto,
                                        String presentacion, int unidadesPorPresentacion, int cantidad,
                                        double precioUnitario, double subtotal) {
}

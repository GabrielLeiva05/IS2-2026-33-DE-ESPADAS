package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * crearFacturaProveedor(numeroFactura, fechaFactura, detalle, idFormaDePago, idProveedor) del diagrama.
 * La forma de pago se agregará con el módulo de Pagos.
 */
public record FacturaProveedorRequestDTO(String idProveedor, Long numeroFactura, LocalDate fechaFactura,
                                         List<RenglonFacturaDTO> detalles) {
}

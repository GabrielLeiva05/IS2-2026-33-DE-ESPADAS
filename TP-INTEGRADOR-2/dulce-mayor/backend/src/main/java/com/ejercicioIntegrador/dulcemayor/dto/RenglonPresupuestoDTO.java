package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.util.List;

/** Renglón del presupuesto con precio vigente y la promoción automática que le corresponde (sin IVA). */
public record RenglonPresupuestoDTO(String idProducto, String nombre, String presentacion, int cantidad,
                                    BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal descuento,
                                    BigDecimal total) {
}

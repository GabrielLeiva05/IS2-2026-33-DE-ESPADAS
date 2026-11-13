package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Presupuesto calculado en el backend con precios y promociones vigentes. Es orientativo: no reserva stock y el
 * precio final se fija al cargar el pedido.
 *
 * @param totalUsd   total convertido con la cotización vigente, o null si no hay cotización
 * @param avisos     ajustes hechos al calcular (stock insuficiente, producto que dejó de estar disponible)
 */
public record PresupuestoDTO(List<RenglonPresupuestoDTO> renglones, BigDecimal subtotal, BigDecimal descuento,
                             BigDecimal total, BigDecimal totalUsd, BigDecimal cotizacion, List<String> avisos) {
}

package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cotización informativa del dólar. Si el proveedor falla, se devuelve el último valor conocido
 * con {@code actualizada = false}: nunca se bloquea el catálogo ni el pedido por esto.
 */
public record CotizacionDolarDTO(BigDecimal valor, LocalDateTime fechaActualizacion, boolean actualizada,
                                 String mensaje) {
}

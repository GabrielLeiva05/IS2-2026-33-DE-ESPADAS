package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Cotización que usa el sistema para mostrar precios en dólares.
 *
 * @param fechaActualizacion cuándo la actualizó la fuente
 * @param consultadaEn       cuándo la consultó Dulce Mayor
 * @param desactualizada     true si la fuente no respondió en la última consulta y se usa la anterior
 */
public record CotizacionDTO(String tipo, String nombre, BigDecimal compra, BigDecimal venta,
                            OffsetDateTime fechaActualizacion, LocalDateTime consultadaEn, boolean desactualizada,
                            String fuente) {
}

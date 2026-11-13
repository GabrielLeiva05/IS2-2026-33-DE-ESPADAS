package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Cotización del dólar que usa el sistema (/api/cotizacion). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CotizacionDTO {

    private static final ZoneId MENDOZA = ZoneId.of("America/Argentina/Mendoza");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String tipo;
    private String nombre;
    private BigDecimal compra;
    private BigDecimal venta;
    private OffsetDateTime fechaActualizacion;
    private boolean desactualizada;
    private String fuente;

    /** "Oficial" → "Dólar Oficial"; el nombre lo informa la fuente. */
    public String getTitulo() {
        String base = nombre == null || nombre.isBlank() ? tipo : nombre;
        return base != null && base.toLowerCase().startsWith("d") ? base : "Dólar " + base;
    }

    /** Fecha de actualización de la fuente, en hora de Mendoza. */
    public String getActualizadaLegible() {
        return fechaActualizacion == null ? null : fechaActualizacion.atZoneSameInstant(MENDOZA).format(FORMATO);
    }
}

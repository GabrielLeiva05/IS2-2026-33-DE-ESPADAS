package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Vigencia de precio (GET /api/vigencias-precio/producto/{id}). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrecioDTO {

    private String id;
    private String fechaDesde;
    private String fechaHasta;
    private double precio;
    private boolean vigente;

    public String getDesdeLegible() {
        return legible(fechaDesde);
    }

    public String getHastaLegible() {
        return fechaHasta == null ? "Vigente" : legible(fechaHasta);
    }

    private static String legible(String fecha) {
        return fecha == null || fecha.length() < 10 ? "" : fecha.substring(8, 10) + "/" + fecha.substring(5, 7) + "/" + fecha.substring(0, 4);
    }
}

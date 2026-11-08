package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Reporte de ventas del período (ReporteVentasDTO de la API). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteAdminDTO {

    private String desde;
    private String hasta;
    private String agrupacion;
    private long pedidos;
    private long presentaciones;
    private BigDecimal importeTotal;
    private List<FilaReporteAdminDTO> filas;
    private Map<String, Long> porEstado;

    public String getAgrupacionLegible() {
        if (agrupacion == null) {
            return "";
        }
        return switch (agrupacion) {
            case "departamento" -> "Departamento";
            case "preventista" -> "Preventista";
            case "cliente" -> "Comercio";
            case "estado" -> "Estado";
            default -> agrupacion;
        };
    }
}

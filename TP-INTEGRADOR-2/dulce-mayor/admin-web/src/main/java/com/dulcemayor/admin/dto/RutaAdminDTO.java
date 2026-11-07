package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Hoja de ruta tal como la devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RutaAdminDTO {

    private String id;
    private String fecha;
    private String idDistribuidor;
    private String nombreDistribuidor;
    private String vehiculo;
    private String patente;
    private String estado;
    private String observacion;
    private List<ParadaAdminDTO> paradas;
    private String idSiguienteParada;
    private BigDecimal totalCobrado;
    private long paradasPendientes;

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "PLANIFICADA" -> "Planificada";
            case "EN_CURSO" -> "En curso";
            case "CERRADA" -> "Cerrada";
            default -> estado;
        };
    }

    public String getClaseEstado() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "EN_CURSO" -> "insignia-azul";
            case "CERRADA" -> "insignia-verde";
            default -> "insignia-gris";
        };
    }
}

package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Parada de la hoja de ruta tal como la devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParadaAdminDTO {

    private String id;
    private String idPedido;
    private String nombreComercial;
    private String calle;
    private String numeracion;
    private String barrio;
    private String localidad;
    private Double latitud;
    private Double longitud;
    private int orden;
    private String estado;
    private String motivoFallo;
    private String observacion;
    private String fechaResolucion;
    private boolean cobroEfectivo;
    private BigDecimal total;
    private String estadoPedido;

    public String getDireccionLegible() {
        return ((calle == null ? "" : calle) + " " + (numeracion == null ? "" : numeracion)).trim()
                + (localidad == null ? "" : " · " + localidad);
    }

    public String getClaseEstado() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "ENTREGADO" -> "insignia-verde";
            case "FALLIDO" -> "insignia-roja";
            default -> "insignia-ambar";
        };
    }
}

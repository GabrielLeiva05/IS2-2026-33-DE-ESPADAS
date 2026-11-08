package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Punto de venta con su responsable y preventista (PuntoVentaResponseDTO de la API). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PuntoVentaAdminDTO {

    private String id;
    private String nombreComercial;
    private String nombreCliente;
    private String apellidoCliente;
    private String idPreventista;
    private String telefono;
    private String emailContacto;
    private DireccionDTO direccion;

    public String getResponsable() {
        return ((nombreCliente == null ? "" : nombreCliente) + " " + (apellidoCliente == null ? "" : apellidoCliente)).trim();
    }

    public String getUbicacion() {
        if (direccion == null) {
            return "";
        }
        String calle = ((direccion.getCalle() == null ? "" : direccion.getCalle()) + " "
                + (direccion.getNumeracion() == null ? "" : direccion.getNumeracion())).trim();
        return direccion.getLocalidad() == null ? calle : calle + " · " + direccion.getLocalidad();
    }
}

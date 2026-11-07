package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sucursal tal como la devuelve la API pública. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SucursalDTO {

    private String id;
    private String nombre;
    private String telefono;
    private String emailContacto;
    private String horario;
    private DireccionDTO direccion;

    public String getDireccionLegible() {
        if (direccion == null) {
            return "";
        }
        String calle = ((direccion.getCalle() == null ? "" : direccion.getCalle()) + " "
                + (direccion.getNumeracion() == null ? "" : direccion.getNumeracion())).trim();
        return direccion.getLocalidad() == null ? calle : calle + " · " + direccion.getLocalidad();
    }

    public boolean isTieneMapa() {
        return direccion != null && direccion.getLatitud() != null && direccion.getLongitud() != null;
    }
}

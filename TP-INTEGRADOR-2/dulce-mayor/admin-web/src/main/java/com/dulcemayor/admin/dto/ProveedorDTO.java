package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Proveedor (GET /api/proveedores). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProveedorDTO {

    private String id;
    private String razonSocial;
    private String cuit;
    private DireccionDTO direccion;

    public String getDireccionLegible() {
        if (direccion == null) {
            return "";
        }
        return direccion.getCalle() + (direccion.getNumeracion() == null ? "" : " " + direccion.getNumeracion())
                + (direccion.getLocalidad() == null ? "" : ", " + direccion.getLocalidad());
    }
}

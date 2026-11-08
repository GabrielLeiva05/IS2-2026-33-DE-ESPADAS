package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Suscripción al newsletter. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuscripcionAdminDTO {

    private String id;
    private String email;
    private boolean activa;
    private String fechaAlta;
    private String fechaBaja;
    private String fechaUltimoEnvio;

    public String getEstadoLegible() {
        return activa ? "Activa" : "Dada de baja";
    }

    public String getClaseEstado() {
        return activa ? "insignia-verde" : "insignia-gris";
    }
}

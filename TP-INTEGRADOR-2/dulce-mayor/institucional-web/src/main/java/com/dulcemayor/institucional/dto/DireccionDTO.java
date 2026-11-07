package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dirección que viene dentro de una sucursal. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DireccionDTO {

    private String calle;
    private String numeracion;
    private String barrio;
    private String localidad;
    private Double latitud;
    private Double longitud;
}

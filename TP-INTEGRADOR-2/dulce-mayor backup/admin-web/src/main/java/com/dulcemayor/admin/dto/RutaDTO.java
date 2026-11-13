package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Ruta del día de un preventista o distribuidor. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RutaDTO {

    private String id;
    private String fecha;
    private String tipoRuta;
    private String idEmpleado;
    private String nombreEmpleado;
    private List<PuntoRutaDTO> puntos;
    private int pendientes;
    private int visitados;
}

package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dirección con su cadena de ubicación completa, tal como la devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DireccionDTO {

    private String id;
    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
    private Double latitud;
    private Double longitud;
    private String idLocalidad;
    private String localidad;
    private String codigoPostal;
    private String idDepartamento;
    private String departamento;
    private String idProvincia;
    private String provincia;
    private String idPais;
    private String pais;

    /** Texto de una línea para listados: "Belgrano 742, Ciudad de Mendoza (Capital), Mendoza". */
    public String getResumen() {
        StringBuilder texto = new StringBuilder();
        texto.append(calle == null ? "" : calle).append(' ').append(numeracion == null ? "" : numeracion);
        if (localidad != null) {
            texto.append(", ").append(localidad);
        }
        if (departamento != null && !departamento.equalsIgnoreCase(localidad)) {
            texto.append(" (").append(departamento).append(')');
        }
        if (provincia != null) {
            texto.append(", ").append(provincia);
        }
        return texto.toString().trim();
    }
}

package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Punto de venta (comercio) tal como lo devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PuntoVentaDTO {

    private String id;
    private String nombreFantasia;
    private String razonSocial;
    private String cuit;
    private String condicionIva;
    private String idCliente;
    private String nombreCliente;
    private DireccionDTO direccion;
}

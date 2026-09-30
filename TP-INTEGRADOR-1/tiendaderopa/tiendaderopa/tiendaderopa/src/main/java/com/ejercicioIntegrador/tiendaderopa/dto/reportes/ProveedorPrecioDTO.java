package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ProveedorPrecioDTO {

    private String idProveedor;
    private String razonSocial;
    private double precioCompra;
    private Date fechaUltimaCompra;
    private String telefonoWhatsapp;
}

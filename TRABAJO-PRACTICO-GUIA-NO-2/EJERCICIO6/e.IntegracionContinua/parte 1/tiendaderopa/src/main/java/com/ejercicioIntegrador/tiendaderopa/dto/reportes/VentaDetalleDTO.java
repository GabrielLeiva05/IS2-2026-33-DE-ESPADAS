package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 DTO para mostrar el detalle de las ventas en los reportes.
 Trabaja con ReporteVentasDTO y ServicioReporteVentas para generar los reportes de ventas.
*/

@Getter
@Setter
@AllArgsConstructor
public class VentaDetalleDTO {

    private String idOrdenCompra;
    private String identificadorCompra;
    private Date fechaCompra;
    private String producto;
    private String categoria;
    private String subCategoria;
    private int cantidad;
    private double subtotal;
    private String formaPago;
}

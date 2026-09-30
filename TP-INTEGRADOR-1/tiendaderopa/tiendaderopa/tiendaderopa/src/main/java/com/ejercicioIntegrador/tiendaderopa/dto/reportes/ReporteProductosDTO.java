package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/* 
 DTO para representar el reporte de productos 
 Trabaja con ServicioReporteProductos y ProductoStockDTO para generar el reporte de productos.
*/

@Getter
@Setter
public class ReporteProductosDTO {

    private int cantidadProductos;
    private int stockTotal;
    private int cantidadBueno;
    private int cantidadRegular;
    private int cantidadMalo;
    private int cantidadSinDefinir;
    private List<ProductoStockDTO> productos;
}

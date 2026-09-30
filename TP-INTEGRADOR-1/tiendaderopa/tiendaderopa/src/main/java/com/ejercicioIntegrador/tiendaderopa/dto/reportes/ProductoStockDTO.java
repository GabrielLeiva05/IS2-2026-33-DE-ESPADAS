package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.Getter;
import lombok.Setter;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoStock;

/*
 DTO para representar el stock de un producto en los reportes.
 Trabaja con ReporteProductosDTO y ServicioReporteProductos para generar el reporte de productos.
*/

@Getter
@Setter
public class ProductoStockDTO {

    private String idProducto;
    private String codigo;
    private String nombre;
    private String categoria;
    private String subCategoria;
    private int stockActual;
    private int stockMaximo;

    /** Null cuando stockMaximo no está configurado (producto nuevo, sin objetivo definido). */
    private Double porcentaje;

    private EstadoStock estado;

    // Solo se completan cuando estado == MALO
    private Integer cantidadSugeridaCompra;
    private String sugerenciaProveedorRazonSocial;
    private String sugerenciaProveedorWhatsappLink;
}

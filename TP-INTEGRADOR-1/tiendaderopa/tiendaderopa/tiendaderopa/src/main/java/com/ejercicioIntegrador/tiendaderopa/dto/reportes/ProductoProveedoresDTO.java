package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductoProveedoresDTO {

    private String idProducto;
    private String codigoProducto;
    private String nombreProducto;

    /** Ordenados por precioCompra ascendente. */
    private List<ProveedorPrecioDTO> proveedores;

    /** El primero de la lista de arriba; null si nunca se le compró este producto a nadie. */
    private ProveedorPrecioDTO proveedorMasEconomico;
}

package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Carga de una factura de proveedor: datos del comprobante y renglones (listas paralelas del formulario). */
@Getter
@Setter
@NoArgsConstructor
public class FacturaProveedorFormularioDTO {

    private String idProveedor;
    private Long numeroFactura;
    private String fechaFactura;
    private List<String> idProducto = new ArrayList<>();
    private List<Integer> cantidad = new ArrayList<>();
    private List<Double> precioUnitario = new ArrayList<>();

    /** Cantidad de renglones a dibujar (al menos uno). */
    public int getRenglones() {
        return Math.max(1, idProducto.size());
    }
}

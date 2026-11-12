package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.model.Proveedor;

import java.time.LocalDate;
import java.util.List;

/** Datos ya resueltos (proveedor y productos como entidades) para crear una factura de proveedor. */
public record DatosFacturaProveedor(Proveedor proveedor, long numeroFactura, LocalDate fechaFactura,
                                    List<RenglonFactura> renglones) {
}

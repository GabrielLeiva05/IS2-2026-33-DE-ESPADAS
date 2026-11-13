package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Factura;
import com.ejercicioIntegrador.dulcemayor.model.FacturaProveedor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory Method — CREADOR CONCRETO de FacturaProveedor (como EnvioCamionCreator del ejemplo).
 * Origen: los datos que carga administración. El número y la fecha son los del comprobante del proveedor.
 */
@Component
public class CreadorConcretoFacturaProveedor extends CreadorFactura<DatosFacturaProveedor> {

    public CreadorConcretoFacturaProveedor(@Value("${app.facturacion.alicuota-iva:0.21}") double alicuotaIva) {
        super(alicuotaIva);
    }

    @Override
    protected Factura crearFactura(DatosFacturaProveedor datos) throws MiException {
        if (datos == null || datos.proveedor() == null) {
            throw new MiException("Debe indicar el proveedor");
        }
        if (datos.numeroFactura() <= 0) {
            throw new MiException("El número de la factura del proveedor debe ser mayor a cero");
        }
        FacturaProveedor factura = new FacturaProveedor(datos.proveedor());
        factura.setNumeroFactura(datos.numeroFactura());
        return factura;
    }

    @Override
    protected List<RenglonFactura> renglones(DatosFacturaProveedor datos) {
        return datos.renglones();
    }

    @Override
    protected LocalDateTime fechaFactura(DatosFacturaProveedor datos) {
        return datos.fechaFactura() == null ? LocalDateTime.now() : datos.fechaFactura().atStartOfDay();
    }
}

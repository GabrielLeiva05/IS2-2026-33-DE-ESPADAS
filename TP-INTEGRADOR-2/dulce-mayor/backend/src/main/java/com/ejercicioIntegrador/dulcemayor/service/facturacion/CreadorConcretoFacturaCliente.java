package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Factura;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory Method — CREADOR CONCRETO de FacturaCliente (como EnvioMotoCreator del ejemplo).
 * Origen: el pedido confirmado. Copia sus renglones con el precio que se reservó al cargarlo y su descuento;
 * la fecha de la factura es la de facturación del pedido (fechaFacturacion, registrada al confirmar).
 */
@Component
public class CreadorConcretoFacturaCliente extends CreadorFactura<Pedido> {

    public CreadorConcretoFacturaCliente(@Value("${app.facturacion.alicuota-iva:0.21}") double alicuotaIva) {
        super(alicuotaIva);
    }

    @Override
    protected Factura crearFactura(Pedido pedido) throws MiException {
        if (pedido == null) {
            throw new MiException("Debe indicar el pedido a facturar");
        }
        return new FacturaCliente(pedido);
    }

    @Override
    protected List<RenglonFactura> renglones(Pedido pedido) {
        return pedido.getDetalles().stream()
                .map(detalle -> new RenglonFactura(detalle.getProducto(), detalle.getCantidad(), detalle.getPrecioUnitario()))
                .toList();
    }

    @Override
    protected double descuento(Pedido pedido) {
        return pedido.getDescuento();
    }

    @Override
    protected LocalDateTime fechaFactura(Pedido pedido) {
        return pedido.getFechaFacturacion() == null ? LocalDateTime.now() : pedido.getFechaFacturacion();
    }
}

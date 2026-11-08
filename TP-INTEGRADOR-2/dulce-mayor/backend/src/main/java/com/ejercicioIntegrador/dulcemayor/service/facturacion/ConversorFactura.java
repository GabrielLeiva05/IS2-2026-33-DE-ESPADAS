package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.dto.DetalleFacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.DetalleFactura;
import com.ejercicioIntegrador.dulcemayor.model.Factura;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.FacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Proveedor;

/** Conversión común de cualquier Factura a su DTO (la usan los dos servicios de facturas). */
public final class ConversorFactura {

    private ConversorFactura() {
    }

    public static FacturaResponseDTO aResponse(Factura factura, double alicuotaIva) {
        String tipo = factura instanceof FacturaProveedor ? "PROVEEDOR" : "CLIENTE";
        Pedido pedido = factura instanceof FacturaCliente fc ? fc.getPedido() : null;
        Cliente cliente = pedido == null ? null : pedido.getPuntoVenta().getCliente();
        Proveedor proveedor = factura instanceof FacturaProveedor fp ? fp.getProveedor() : null;
        return new FacturaResponseDTO(factura.getId(), tipo, factura.getNumeroFactura(), factura.getFechaFactura(),
                factura.getEstado(), factura.getSubtotal(), factura.getDescuento(), factura.getIva(), alicuotaIva,
                factura.getTotal(),
                pedido == null ? null : pedido.getId(), pedido == null ? null : pedido.getNumeroPedido(),
                cliente == null ? null : cliente.getId(),
                cliente == null ? null : (cliente.getNombre() + " " + cliente.getApellido()).trim(),
                pedido == null ? null : pedido.getPuntoVenta().getNombreFantasia(),
                proveedor == null ? null : proveedor.getId(), proveedor == null ? null : proveedor.getRazonSocial(),
                proveedor == null ? null : proveedor.getCuit(),
                factura.getDetalles().stream().map(ConversorFactura::detalle).toList(),
                factura.isEliminado());
    }

    private static DetalleFacturaResponseDTO detalle(DetalleFactura d) {
        Producto p = d.getProducto();
        return new DetalleFacturaResponseDTO(d.getId(), p.getId(), p.getCodigo(), p.getNombre(),
                p.getPresentacion() == null ? null : p.getPresentacion().name(), p.getUnidadesPorPresentacion(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }
}

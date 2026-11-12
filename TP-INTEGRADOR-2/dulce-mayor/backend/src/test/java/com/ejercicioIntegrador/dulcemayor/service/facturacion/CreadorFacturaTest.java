package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.DetallePedido;
import com.ejercicioIntegrador.dulcemayor.model.Factura;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.FacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Proveedor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Factory Method de facturas: cada creador concreto instancia su producto (FacturaCliente / FacturaProveedor)
 * y la operación común emitir() calcula subtotal, IVA discriminado y total.
 */
class CreadorFacturaTest {

    private static final double DELTA = 0.001;

    private static Producto producto(String id) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        return producto;
    }

    private static Pedido pedido(double descuento) {
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setDescuento(descuento);
        pedido.setFechaFacturacion(LocalDateTime.of(2026, 11, 5, 10, 30));
        pedido.agregarDetalle(detalle(producto("A"), 4, 1000.0));
        pedido.agregarDetalle(detalle(producto("B"), 2, 250.5));
        return pedido;
    }

    private static DetallePedido detalle(Producto producto, int cantidad, double precio) {
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(cantidad * precio);
        return detalle;
    }

    @Test
    void elCreadorDeClienteInstanciaUnaFacturaClienteDelPedidoConIvaDiscriminado() throws MiException {
        Pedido pedido = pedido(0);

        Factura factura = new CreadorFacturaCliente(0.21).emitir(pedido);

        assertTrue(factura instanceof FacturaCliente);
        assertSame(pedido, ((FacturaCliente) factura).getPedido());
        assertEquals(2, factura.getDetalles().size());
        assertEquals(4501.0, factura.getSubtotal(), DELTA);      // 4 x 1000 + 2 x 250,50 (sin IVA)
        assertEquals(945.21, factura.getIva(), DELTA);           // 21 % de 4501
        assertEquals(5446.21, factura.getTotal(), DELTA);        // total con IVA
        assertEquals(EstadoFactura.PENDIENTE_PAGO, factura.getEstado());
        assertEquals(pedido.getFechaFacturacion(), factura.getFechaFactura());
    }

    @Test
    void elDescuentoDelPedidoSeRestaAntesDelIva() throws MiException {
        Factura factura = new CreadorFacturaCliente(0.21).emitir(pedido(501.0));

        assertEquals(4501.0, factura.getSubtotal(), DELTA);
        assertEquals(501.0, factura.getDescuento(), DELTA);
        assertEquals(840.0, factura.getIva(), DELTA);            // 21 % de 4000
        assertEquals(4840.0, factura.getTotal(), DELTA);
    }

    @Test
    void unPedidoSinRenglonesNoSeFactura() {
        Pedido vacio = new Pedido();
        assertThrows(MiException.class, () -> new CreadorFacturaCliente(0.21).emitir(vacio));
    }

    @Test
    void elCreadorDeProveedorInstanciaUnaFacturaProveedor() throws MiException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId("prov-1");
        DatosFacturaProveedor datos = new DatosFacturaProveedor(proveedor, 4521L, LocalDate.of(2026, 11, 2),
                List.of(new RenglonFactura(producto("A"), 10, 700.0), new RenglonFactura(producto("B"), 5, 120.4)));

        Factura factura = new CreadorFacturaProveedor(0.21).emitir(datos);

        assertTrue(factura instanceof FacturaProveedor);
        assertSame(proveedor, ((FacturaProveedor) factura).getProveedor());
        assertEquals(4521L, factura.getNumeroFactura());
        assertEquals(7602.0, factura.getSubtotal(), DELTA);
        assertEquals(0.0, factura.getDescuento(), DELTA);
        assertEquals(9198.42, factura.getTotal(), DELTA);
        assertEquals(LocalDate.of(2026, 11, 2).atStartOfDay(), factura.getFechaFactura());
    }

    @Test
    void laFacturaDeProveedorExigeNumeroPositivoYRenglonesValidos() {
        Proveedor proveedor = new Proveedor();
        CreadorFacturaProveedor creador = new CreadorFacturaProveedor(0.21);
        assertThrows(MiException.class, () -> creador.emitir(new DatosFacturaProveedor(proveedor, 0L, LocalDate.now(),
                List.of(new RenglonFactura(producto("A"), 1, 10.0)))));
        assertThrows(MiException.class, () -> creador.emitir(new DatosFacturaProveedor(proveedor, 1L, LocalDate.now(),
                List.of(new RenglonFactura(producto("A"), 0, 10.0)))));
        assertThrows(MiException.class, () -> creador.emitir(new DatosFacturaProveedor(proveedor, 1L, LocalDate.now(),
                List.of(new RenglonFactura(producto("A"), 1, 0.0)))));
        assertThrows(MiException.class, () -> creador.emitir(new DatosFacturaProveedor(null, 1L, LocalDate.now(),
                List.of(new RenglonFactura(producto("A"), 1, 10.0)))));
    }

    @Test
    void laAlicuotaDebeEstarEntreCeroYUno() {
        assertThrows(IllegalArgumentException.class, () -> new CreadorFacturaCliente(21));
        assertThrows(IllegalArgumentException.class, () -> new CreadorFacturaProveedor(-0.1));
    }
}

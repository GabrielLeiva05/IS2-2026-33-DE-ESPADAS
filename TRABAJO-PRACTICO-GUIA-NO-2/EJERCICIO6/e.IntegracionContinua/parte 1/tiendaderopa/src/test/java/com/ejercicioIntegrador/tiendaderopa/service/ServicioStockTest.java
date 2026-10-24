package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.MovimientoInventario;
import com.ejercicioIntegrador.tiendaderopa.model.ObjetivoReposicion;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import com.ejercicioIntegrador.tiendaderopa.model.TipoMovimientoInventario;
import com.ejercicioIntegrador.tiendaderopa.repository.MovimientoInventarioRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.ObjetivoReposicionRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioStockTest {

    private ServicioStock servicio;
    private ObjetivoReposicionRepositorio objetivos;
    private MovimientoInventarioRepositorio movimientos;
    private RepositorioStock stockRepositorio;
    private ObjetivoReposicion saldo;
    private Sucursal sucursal;
    private Producto producto;
    private Set<String> referenciasRegistradas;

    @BeforeEach
    void setUp() {
        servicio = new ServicioStock();
        objetivos = mock(ObjetivoReposicionRepositorio.class);
        movimientos = mock(MovimientoInventarioRepositorio.class);
        referenciasRegistradas = new HashSet<>();
        sucursal = new Sucursal();
        sucursal.setId("sucursal-1");
        producto = new Producto();
        producto.setId("producto-1");
        saldo = new ObjetivoReposicion();
        saldo.setCantidadActual(8);
        saldo.setCantidadObjetivo(20);

        stockRepositorio = mock(RepositorioStock.class);
        ReflectionTestUtils.setField(servicio, "repositorio", stockRepositorio);
        ReflectionTestUtils.setField(servicio, "objetivoRepositorio", objetivos);
        ReflectionTestUtils.setField(servicio, "movimientoRepositorio", movimientos);
        ReflectionTestUtils.setField(servicio, "servicioSucursal", mock(ServicioSucursal.class));
        ReflectionTestUtils.setField(servicio, "servicioDetalleOrdenCompraProveedor",
                mock(ServicioDetalleOrdenCompraProveedor.class));
        when(objetivos.bloquearPorSucursalYProducto("sucursal-1", "producto-1"))
                .thenReturn(Optional.of(saldo));
        when(objetivos.save(any(ObjetivoReposicion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientos.existsByTipoAndReferencia(any(), any()))
                .thenAnswer(invocation -> referenciasRegistradas.contains(clave(
                        invocation.getArgument(0), invocation.getArgument(1))));
        when(movimientos.save(any(MovimientoInventario.class))).thenAnswer(invocation -> {
            MovimientoInventario movimiento = invocation.getArgument(0);
            referenciasRegistradas.add(clave(movimiento.getTipo(), movimiento.getReferencia()));
            return movimiento;
        });
    }

    @Test
    void pagoDescuentaUnidadesUnaSolaVez() {
        OrdenCompra orden = new OrdenCompra();
        orden.setSucursal(sucursal);
        orden.setIdentificadorCompra("ORD-1");
        DetalleCompra detalle = new DetalleCompra();
        detalle.setId("detalle-venta-1");
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        orden.getDetalles().add(detalle);

        servicio.registrarVenta(orden);
        servicio.registrarVenta(orden);

        assertEquals(5, saldo.getCantidadActual());
        verify(objetivos, times(1)).save(saldo);
        verify(movimientos, times(1)).save(any(MovimientoInventario.class));
    }

    @Test
    void recepcionDeProveedorSumaUnidades() {
        OrdenCompraProveedor orden = new OrdenCompraProveedor();
        orden.setId("orden-proveedor-1");
        orden.setSucursal(sucursal);
        DetalleOrdenCompraProveedor detalle = new DetalleOrdenCompraProveedor();
        detalle.setId("detalle-proveedor-1");
        detalle.setProducto(producto);
        detalle.setCantidad(4);
        ServicioDetalleOrdenCompraProveedor detalleService =
                (ServicioDetalleOrdenCompraProveedor) ReflectionTestUtils.getField(
                        servicio, "servicioDetalleOrdenCompraProveedor");
        when(detalleService.listarPorOrden("orden-proveedor-1")).thenReturn(List.of(detalle));

        servicio.registrarRecepcion(orden);

        assertEquals(12, saldo.getCantidadActual());
        verify(movimientos).save(any(MovimientoInventario.class));
    }

    @Test
    void anularStockYaEliminadoNoRepiteAjustes() throws Exception {
        Stock stock = new Stock();
        stock.setEliminado(true);
        when(stockRepositorio.findById("stock-1")).thenReturn(Optional.of(stock));

        servicio.eliminarStock("stock-1", "Anulación duplicada");

        verify(objetivos, never()).save(any(ObjetivoReposicion.class));
        verify(movimientos, never()).save(any(MovimientoInventario.class));
        verify(stockRepositorio, never()).save(any(Stock.class));
    }

    private String clave(TipoMovimientoInventario tipo, String referencia) {
        return tipo + ":" + referencia;
    }
}
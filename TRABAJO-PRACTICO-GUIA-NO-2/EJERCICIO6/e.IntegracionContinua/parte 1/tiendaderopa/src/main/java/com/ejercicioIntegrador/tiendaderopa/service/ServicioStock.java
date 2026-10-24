package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class ServicioStock {

    @Autowired
    private RepositorioStock repositorio;
    
    @Autowired
    private ServicioFactura svcFactura;

    @Autowired
    private ServicioSucursal servicioSucursal;

    @Autowired
    private ServicioDetalleOrdenCompraProveedor servicioDetalleOrdenCompraProveedor;

    @Autowired
    private ObjetivoReposicionRepositorio objetivoRepositorio;

    @Autowired
    private MovimientoInventarioRepositorio movimientoRepositorio;

    /** Solo lectura — no guarda nada, reusable desde crearStock. */
    public void validar(String idDetalleFactura) throws Exception {
        DetalleFactura detalle = svcFactura.buscarDetalleFactura(idDetalleFactura);
        Factura factura = detalle.getFactura();

        if (factura.getEstadoFactura() != EstadoFactura.PAGADA) {
            throw new Exception("Solo se puede generar stock para una factura ya pagada");
        }
        if (repositorio.existsByDetalleFactura_IdAndEliminadoFalse(idDetalleFactura)) {
            throw new Exception("Ya existe un movimiento de stock activo para este detalle de factura");
        }

        int signo = obtenerSignoMovimientoStock(factura);
        Sucursal sucursal = servicioSucursal.obtenerPrincipal();
        int cantidadResultante = cantidadActual(detalle.getProducto().getId(), sucursal.getId())
            + (signo * detalle.getCantidad());
        if (cantidadResultante < 0) {
            throw new Exception("No hay stock suficiente para completar esta operación");
        }
    }

    @Transactional
    public void crearStock(String idDetalleFactura) throws Exception {
        validar(idDetalleFactura);

        DetalleFactura detalle = svcFactura.buscarDetalleFactura(idDetalleFactura);
        int signo = obtenerSignoMovimientoStock(detalle.getFactura());
        Sucursal sucursal = servicioSucursal.obtenerPrincipal();
        int movimiento = signo * detalle.getCantidad();
        ObjetivoReposicion saldo = bloquearSaldo(detalle.getProducto(), sucursal);
        int actual = saldo.getCantidadActual() + movimiento;
        if (actual < 0) {
            throw new IllegalStateException("No hay stock suficiente para completar esta operación");
        }
        saldo.setCantidadActual(actual);
        objetivoRepositorio.save(saldo);

        Stock stock = new Stock();
        stock.setDetalleFactura(detalle);
        stock.setCantActual(actual);
        stock.setEliminado(false);
        stock.setObservacion("Movimiento de factura " + detalle.getFactura().getNumeroFactura());
        repositorio.save(stock);
        guardarEvento(detalle.getProducto(), sucursal, movimiento, TipoMovimientoInventario.FACTURA,
                detalle.getId(), stock.getObservacion());
    }

    @Transactional
    public void registrarVenta(OrdenCompra orden) {
        Sucursal sucursal = orden.getSucursal() == null
                ? servicioSucursal.obtenerPrincipal()
                : orden.getSucursal();
        orden.setSucursal(sucursal);
        for (DetalleCompra detalle : orden.getDetalles()) {
            if (detalle.isEliminado()) {
                continue;
            }
            registrarMovimiento(detalle.getProducto(), sucursal, -detalle.getCantidad(),
                    TipoMovimientoInventario.VENTA, detalle.getId(),
                    "Venta web " + orden.getIdentificadorCompra());
        }
    }

    @Transactional
    public void registrarRecepcion(OrdenCompraProveedor orden) {
        Sucursal sucursal = orden.getSucursal() == null
                ? servicioSucursal.obtenerPrincipal()
                : orden.getSucursal();
        orden.setSucursal(sucursal);
        for (DetalleOrdenCompraProveedor detalle : servicioDetalleOrdenCompraProveedor.listarPorOrden(orden.getId())) {
            if (detalle.isEliminado()) {
                continue;
            }
            registrarMovimiento(detalle.getProducto(), sucursal, detalle.getCantidad(),
                    TipoMovimientoInventario.RECEPCION_PROVEEDOR, detalle.getId(),
                    "Recepción de orden proveedor " + orden.getId());
        }
    }

    private void registrarMovimiento(Producto producto, Sucursal sucursal, int cantidad,
            TipoMovimientoInventario tipo, String referencia, String observacion) {
        if (movimientoRepositorio.existsByTipoAndReferencia(tipo, referencia)) {
            return;
        }
        ObjetivoReposicion saldo = bloquearSaldo(producto, sucursal);
        saldo.setCantidadActual(saldo.getCantidadActual() + cantidad);
        objetivoRepositorio.save(saldo);
        guardarEvento(producto, sucursal, cantidad, tipo, referencia, observacion);
    }

    private void guardarEvento(Producto producto, Sucursal sucursal, int cantidad,
            TipoMovimientoInventario tipo, String referencia, String observacion) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setSucursal(sucursal);
        movimiento.setCantidad(cantidad);
        movimiento.setTipo(tipo);
        movimiento.setReferencia(referencia);
        movimiento.setObservacion(observacion);
        movimientoRepositorio.save(movimiento);
    }

    private ObjetivoReposicion bloquearSaldo(Producto producto, Sucursal sucursal) {
        return objetivoRepositorio.bloquearPorSucursalYProducto(sucursal.getId(), producto.getId())
                .orElseGet(() -> {
                    ObjetivoReposicion saldo = new ObjetivoReposicion();
                    saldo.setSucursal(sucursal);
                    saldo.setProducto(producto);
                    saldo.setCantidadActual(stockBase(producto.getId(), sucursal));
                    saldo.setCantidadObjetivo(Math.max(producto.getStockMaximo(), 0));
                    return objetivoRepositorio.save(saldo);
                });
    }

    @Transactional
    public void eliminarStock(String id, String observacion) throws Exception {

        Stock stock = buscarStock(id);
        if (stock.isEliminado()) {
            return;
        }
        DetalleFactura detalle = stock.getDetalleFactura();
        Producto producto = detalle.getProducto();
        Factura factura = detalle.getFactura();
        int movimiento = obtenerSignoMovimientoStock(factura) * detalle.getCantidad();
        Sucursal sucursal = servicioSucursal.obtenerPrincipal();
        registrarMovimiento(producto, sucursal, -movimiento, TipoMovimientoInventario.ANULACION,
            stock.getId(), "Anulación del movimiento " + stock.getId());

        List<Stock> posteriores = repositorio
            .findByDetalleFactura_Producto_IdAndEliminadoFalseAndFechaMovimientoAfterOrderByFechaMovimientoAsc(
                producto.getId(), stock.getFechaMovimiento());

        // 4. Al eliminar este movimiento tenemos que DESHACER su efecto en los posteriores
        int correccion = -movimiento;
        /*
         * 4. Al eliminar este movimiento tenemos que DESHACER
         *    su efecto en todos los movimientos posteriores.
         *
         *    Si movimiento = -3:
         *        -(-3) = +3
         *
         *    Si movimiento = +5:
         *        -(+5) = -5
         */

        for (Stock posterior : posteriores) {

            posterior.setCantActual(
                    posterior.getCantActual() + correccion
            );

            repositorio.save(posterior);
        }

        // 5. Finalmente anulamos el movimiento original
        stock.setEliminado(true);
        stock.setObservacion(observacion);

        repositorio.save(stock);
    }

    public Stock buscarStock(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe el movimiento de stock con id " + id));
    }

    public Collection<Stock> listarStock() {
        return repositorio.findByEliminadoFalse();
    }

    public Stock buscarStockActual(String idProducto) {
        List<ObjetivoReposicion> saldos = objetivoRepositorio.findByProducto_Id(idProducto);
        if (saldos.isEmpty()) {
            return repositorio.findTopByDetalleFactura_Producto_IdAndEliminadoFalseOrderByFechaMovimientoDesc(idProducto)
                    .orElse(null);
        }
        Stock total = new Stock();
        total.setCantActual(saldos.stream().mapToInt(ObjetivoReposicion::getCantidadActual).sum());
        return total;
    }

    @Transactional(readOnly = true)
    public int cantidadActual(String idProducto, String sucursalId) {
        return objetivoRepositorio.findBySucursal_IdAndProducto_Id(sucursalId, idProducto)
                .map(ObjetivoReposicion::getCantidadActual)
                .orElseGet(() -> {
                    Sucursal sucursal = servicioSucursal.buscarActiva(sucursalId);
                    return stockBase(idProducto, sucursal);
                });
    }

    private int stockBase(String idProducto, Sucursal sucursal) {
        if (!sucursal.isPrincipal()) {
            return 0;
        }
        return repositorio.findTopByDetalleFactura_Producto_IdAndEliminadoFalseOrderByFechaMovimientoDesc(idProducto)
                .map(Stock::getCantActual)
                .orElse(0);
    }

    /**
     * Regla de negocio en el Servicio:
     * Evalúa la subclase concreta de Factura para determinar si el movimiento suma o resta stock.
     */
    private int obtenerSignoMovimientoStock(Factura factura) {
        if (factura instanceof FacturaCliente) {
            return -1; // Venta: resta stock
        } else if (factura instanceof FacturaProveedor) {
            return 1;  // Compra: suma stock
        }
        throw new IllegalArgumentException("Tipo de factura no soportado para movimiento de stock");
    }

//    public boolean puedeGenerarStock( String idFactura) throws Exception{
//        Factura factura = svcFactura.buscarFactura(idFactura);
//        return factura.getEstadoFactura() == EstadoFactura.PAGADA;
//    }
}
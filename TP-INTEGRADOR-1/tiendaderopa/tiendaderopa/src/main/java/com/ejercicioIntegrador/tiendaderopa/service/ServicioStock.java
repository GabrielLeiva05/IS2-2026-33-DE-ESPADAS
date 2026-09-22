package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
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
        int cantidadResultante = calcularStockActual(detalle.getProducto().getId()) + (signo * detalle.getCantidad());
        if (cantidadResultante < 0) {
            throw new Exception("No hay stock suficiente para completar esta operación");
        }
    }

    @Transactional
    public void crearStock(String idDetalleFactura) throws Exception {
        validar(idDetalleFactura);

        DetalleFactura detalle = svcFactura.buscarDetalleFactura(idDetalleFactura);
        int signo = obtenerSignoMovimientoStock(detalle.getFactura());
        int cantidadNueva = calcularStockActual(detalle.getProducto().getId()) + (signo * detalle.getCantidad());

        Stock nuevo = new Stock();
        nuevo.setDetalleFactura(detalle);
        nuevo.setCantActual(cantidadNueva);
        nuevo.setEliminado(false);

        repositorio.save(nuevo);
    }

    @Transactional
    public void eliminarStock(String id, String observacion) throws Exception {

        // 1. Buscamos el movimiento que queremos anular
        Stock stock = buscarStock(id);

        String idProducto = stock.getDetalleFactura()
                .getProducto()
                .getId();

        // 2. Obtenemos el efecto que tuvo este movimiento sobre el stock
        Factura factura = stock.getDetalleFactura().getFactura();

        int movimiento = obtenerSignoMovimientoStock(factura)
                * stock.getDetalleFactura().getCantidad();

        // 3. Buscamos todos los movimientos posteriores
        List<Stock> posteriores =
                repositorio
                        .findByDetalleFactura_Producto_IdAndEliminadoFalseAndFechaMovimientoAfterOrderByFechaMovimientoAsc(
                                idProducto,
                                stock.getFechaMovimiento()
                        );

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
        return repositorio
                .findTopByDetalleFactura_Producto_IdAndEliminadoFalseOrderByFechaMovimientoDesc(idProducto)
                .orElse(null);
    }

    private int calcularStockActual(String idProducto) {
        Stock ultimo = buscarStockActual(idProducto);
        return (ultimo != null) ? ultimo.getCantActual() : 0;
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
}
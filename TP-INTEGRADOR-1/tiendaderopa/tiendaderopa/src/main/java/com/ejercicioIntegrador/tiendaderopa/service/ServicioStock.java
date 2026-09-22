package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Solo habla con ServicioFactura para llegar a DetalleFactura/Producto/
 * Factura — nunca con RepositorioDetalleFactura ni RepositorioFactura
 * directo. Esto evita la dependencia circular: si ServicioStock llamara
 * a ServicioFactura Y ServicioFactura llamara de vuelta a ServicioStock,
 * quedarían enroscados entre sí. Por eso quien orquesta la secuencia
 * completa (crear detalle -> marcar pagada -> generar stock) es el
 * Controller, no ninguno de los dos Services.
 */
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

        // Confirmado con el enunciado: el stock se toca cuando el pago ya
        // está confirmado, no antes de eso (evita descontar/sumar stock
        // de una operación que todavía puede anularse).
        if (factura.getEstadoFactura() != EstadoFactura.PAGADA) {
            throw new Exception("Solo se puede generar stock para una factura ya pagada");
        }
        if (repositorio.existsByDetalleFactura_IdAndEliminadoFalse(idDetalleFactura)) {
            throw new Exception("Ya existe un movimiento de stock activo para este detalle de factura");
        }

        int signo = factura.getSignoMovimientoStock(); // +1 Proveedor, -1 Cliente (polimórfico)
        int cantidadResultante = calcularStockActual(detalle.getProducto().getId()) + (signo * detalle.getCantidad());
        if (cantidadResultante < 0) {
            throw new Exception("No hay stock suficiente para completar esta operación");
        }
    }

    @Transactional
    public void crearStock(String idDetalleFactura) throws Exception {
        validar(idDetalleFactura);

        DetalleFactura detalle = svcFactura.buscarDetalleFactura(idDetalleFactura);
        int signo = detalle.getFactura().getSignoMovimientoStock();
        int cantidadNueva = calcularStockActual(detalle.getProducto().getId()) + (signo * detalle.getCantidad());

        Stock nuevo = new Stock();
        nuevo.setDetalleFactura(detalle);
        nuevo.setCantActual(cantidadNueva);
        nuevo.setEliminado(false);
        // fechaMovimiento se completa sola

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

        int movimiento = factura.getSignoMovimientoStock()
                * stock.getDetalleFactura().getCantidad();

        /*
         * Ejemplos:
         *
         * Venta de 3:
         * movimiento = -1 * 3 = -3
         *
         * Compra de 5:
         * movimiento = +1 * 5 = +5
         */

        // 3. Buscamos todos los movimientos posteriores
        List<Stock> posteriores =
                repositorio
                        .findByDetalleFactura_Producto_IdAndEliminadoFalseAndFechaMovimientoAfterOrderByFechaMovimientoAsc(
                                idProducto,
                                stock.getFechaMovimiento()
                        );

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
        int correccion = -movimiento;

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
}
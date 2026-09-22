package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ServicioOrdenCompra {

    @Autowired
    private RepositorioOrdenCompra repositorioOrdenCompra;

    @Autowired
    private ServicioProducto servicioProducto;

    @Autowired
    private ServicioDetalleCompra servicioDetalleCompra;

    public List<OrdenCompra> listarTodas() {
        return repositorioOrdenCompra.findAll();
    }

    public List<OrdenCompra> listarActivas() {
        return repositorioOrdenCompra.findByEliminadoFalse();
    }

    public List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado) {
        return repositorioOrdenCompra.findByEstadoOrdenCompraAndEliminadoFalse(estado);
    }

    public OrdenCompra buscarPorId(String id) {
        return repositorioOrdenCompra.findById(id)
                .filter(o -> !o.isEliminado())
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con ID: " + id));
    }

    @Transactional
    public OrdenCompra crearOrdenCompra(String identificadorCompra) {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdentificadorCompra(identificadorCompra);
        orden.setFecha(new Date());
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR);
        orden.setTotal(0.0);
        orden.setEliminado(false);

        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra agregarDetalleAOrden(String ordenId, String productoId, int cantidad, double precioUnitario) {
        OrdenCompra orden = buscarPorId(ordenId);
        Producto producto = servicioProducto.buscarPorId(productoId); // Delega la búsqueda al servicio de Producto

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(cantidad * precioUnitario);
        detalle.setOrdenCompra(orden);
        detalle.setEliminado(false);

        orden.getDetalles().add(detalle);
        recalcularTotal(orden);

        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra cambiarEstado(String id, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = buscarPorId(id);
        orden.setEstadoOrdenCompra(nuevoEstado);
        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public void eliminarOrdenCompra(String id) {
        OrdenCompra orden = buscarPorId(id);
        orden.setEliminado(true);
        for (DetalleCompra detalle : orden.getDetalles()) {
            servicioDetalleCompra.eliminarDetalleCompra(detalle.getId()); // Delega el borrado lógico al servicio de Detalle
        }
        repositorioOrdenCompra.save(orden);
    }

    private void recalcularTotal(OrdenCompra orden) {
        double total = orden.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .mapToDouble(DetalleCompra::getSubtotal)
                .sum();
        orden.setTotal(total);
    }
}
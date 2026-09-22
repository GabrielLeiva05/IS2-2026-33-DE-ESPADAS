package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioDetalleCompra {

    @Autowired
    private RepositorioDetalleCompra repositorioDetalleCompra;

    @Autowired
    private ServicioProducto servicioProducto; // Comunicación Servicio a Servicio

    public List<DetalleCompra> listarTodos() {
        return repositorioDetalleCompra.findByEliminadoFalse();
    }

    public List<DetalleCompra> listarPorOrden(String ordenId) {
        return repositorioDetalleCompra.findByOrdenCompraIdAndEliminadoFalse(ordenId);
    }

    public DetalleCompra buscarPorId(String id) {
        return repositorioDetalleCompra.findById(id)
                .filter(d -> !d.isEliminado())
                .orElseThrow(() -> new RuntimeException("Detalle de compra no encontrado con ID: " + id));
    }

    public DetalleCompra crearDetalleCompra(String idProducto, int cantidad, double precioUnitario) {
        Producto producto = servicioProducto.buscarPorId(idProducto); // Delega la búsqueda al servicio de Producto

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(cantidad * precioUnitario);
        detalle.setEliminado(false);

        return repositorioDetalleCompra.save(detalle);
    }

    public void eliminarDetalleCompra(String id) {
        DetalleCompra detalle = buscarPorId(id);
        detalle.setEliminado(true);
        repositorioDetalleCompra.save(detalle);
    }
}
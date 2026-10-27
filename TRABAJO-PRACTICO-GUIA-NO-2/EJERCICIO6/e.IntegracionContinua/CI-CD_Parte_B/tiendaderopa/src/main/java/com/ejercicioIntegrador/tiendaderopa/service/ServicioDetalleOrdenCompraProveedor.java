package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleOrdenCompraProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioDetalleOrdenCompraProveedor {

    @Autowired
    private RepositorioDetalleOrdenCompraProveedor repositorio;
    @Autowired
    private ServicioProducto svcProducto;

    @Transactional
    public DetalleOrdenCompraProveedor crear(OrdenCompraProveedor orden, String idProducto,
                                              int cantidad, double precioCompra) {
        Producto producto = svcProducto.buscarPorId(idProducto);

        DetalleOrdenCompraProveedor detalle = new DetalleOrdenCompraProveedor();
        detalle.setOrdenCompraProveedor(orden);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioCompra(precioCompra);
        detalle.setEliminado(false);
        return repositorio.save(detalle);
    }

    public List<DetalleOrdenCompraProveedor> listarPorOrden(String idOrden) {
        return repositorio.findByOrdenCompraProveedor_Id(idOrden);
    }

    /**
     * Todas las compras activas hechas a proveedores por un producto dado,
     * de la más económica a la más cara. Usado por ServicioOrdenCompraProveedor
     * (que a su vez lo expone a ServicioReporteProveedores).
     */
    public List<DetalleOrdenCompraProveedor> buscarPorProductoOrdenadoPorPrecio(String idProducto) {
        return repositorio.buscarPorProductoOrdenadoPorPrecio(idProducto);
    }
}
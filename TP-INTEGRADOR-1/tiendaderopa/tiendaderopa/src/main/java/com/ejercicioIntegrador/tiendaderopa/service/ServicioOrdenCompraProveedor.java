package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.ItemCompraDTO;
import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompraProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * ARCHIVO NUEVO.
 *
 * A propósito, esta clase NO conoce a ServicioFacturaProveedor. Si lo
 * hiciera, y ServicioFacturaProveedor a su vez necesitara volver a
 * preguntarle algo a esta clase, quedarían dos Services llamándose en
 * círculo. La creación conjunta de la Orden + su Factura se resuelve en
 * ControladorOrdenCompraProveedor (ver ese archivo), no acá.
 *
 * Solo se comunica con otros Services (ServicioProveedor, ServicioProducto),
 * nunca con RepositorioProveedor ni RepositorioProducto directo — regla
 * de capas: Service -> Service, cada Service solo con su propio Repository.
 */
@Service
public class ServicioOrdenCompraProveedor {

    @Autowired
    private RepositorioOrdenCompraProveedor repositorio;
    @Autowired
    private RepositorioDetalleOrdenCompraProveedor repositorioDetalle;
    @Autowired
    private ServicioProveedor svcProveedor;
    @Autowired
    private ServicioProducto svcProducto;

    public void validar(String idProveedor, List<ItemCompraDTO> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new Exception("La orden de compra necesita al menos un producto");
        }
        svcProveedor.buscarProveedor(idProveedor); // valida que exista
        for (ItemCompraDTO item : items) {
            if (item.getCantidad() <= 0) {
                throw new Exception("La cantidad debe ser mayor a cero para cada producto");
            }
            if (item.getPrecioCompra() <= 0) {
                throw new Exception("El precio de compra debe ser mayor a cero");
            }
        }
    }

    @Transactional
    public OrdenCompraProveedor crearOrdenCompraProveedor(String idProveedor, List<ItemCompraDTO> items) throws Exception {
        validar(idProveedor, items);

        Proveedor proveedor = svcProveedor.buscarProveedor(idProveedor);

        OrdenCompraProveedor orden = new OrdenCompraProveedor();
        orden.setProveedor(proveedor);
        orden.setFecha(new Date());
        orden.setEstado(EstadoOrdenCompraProveedor.PENDIENTE);
        orden.setEliminado(false);
        orden = repositorio.save(orden); // se guarda primero para tener id y poder asociar el detalle

        double total = 0;
        for (ItemCompraDTO item : items) {
            Producto producto = svcProducto.buscarPorId(item.getIdProducto());

            DetalleOrdenCompraProveedor detalle = new DetalleOrdenCompraProveedor();
            detalle.setOrdenCompraProveedor(orden);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioCompra(item.getPrecioCompra());
            detalle.setEliminado(false);
            repositorioDetalle.save(detalle);

            total += item.getCantidad() * item.getPrecioCompra();
        }

        orden.setTotal(total);
        return repositorio.save(orden);
    }

    @Transactional
    public void marcarComoEntregada(String idOrden) throws Exception {
        OrdenCompraProveedor orden = buscarOrdenCompraProveedor(idOrden);
        if (orden.getEstado() != EstadoOrdenCompraProveedor.PENDIENTE) {
            throw new Exception("Solo una orden PENDIENTE puede marcarse como entregada");
        }
        orden.setEstado(EstadoOrdenCompraProveedor.ENTREGADA);
        repositorio.save(orden);
    }

    /**
     * Solo cambia el estado de la ORDEN. La anulación de la FACTURA
     * asociada (y la validación de que no esté pagada) vive en el
     * Controller, para no generar el ciclo Service-Service que se evita
     * en toda esta clase.
     */
    @Transactional
    public void anularOrdenCompraProveedor(String idOrden) throws Exception {
        OrdenCompraProveedor orden = buscarOrdenCompraProveedor(idOrden);
        if (orden.getEstado() == EstadoOrdenCompraProveedor.ENTREGADA) {
            throw new Exception("No se puede anular una orden ya entregada");
        }
        orden.setEstado(EstadoOrdenCompraProveedor.ANULADA);
        repositorio.save(orden);
    }

    public OrdenCompraProveedor buscarOrdenCompraProveedor(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe la orden de compra con id " + id));
    }

    public List<DetalleOrdenCompraProveedor> listarDetallePorOrden(String idOrden) {
        return repositorioDetalle.findByOrdenCompraProveedor_Id(idOrden);
    }

    public Collection<OrdenCompraProveedor> listarOrdenCompraProveedor() {
        return repositorio.findByEliminadoFalse();
    }
}

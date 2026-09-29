package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ServicioOrdenCompra {

    @Autowired
    private RepositorioOrdenCompra repositorioOrdenCompra;

    @Autowired
    private ServicioProducto servicioProducto;

    @Autowired
    private ServicioDetalleCompra servicioDetalleCompra;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarTodas() {
        return repositorioOrdenCompra.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarActivas() {
        return repositorioOrdenCompra.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> listarActivasDeUsuario(String email) throws Exception {
        Usuario usuario = usuarioServicio.buscarActivoPorNombreUsuario(email);
        return repositorioOrdenCompra.findByUsuario_IdAndEliminadoFalse(usuario.getId());
    }

    public List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado) {
        return repositorioOrdenCompra.findByEstadoOrdenCompraAndEliminadoFalse(estado);
    }

    public OrdenCompra buscarPorId(String id) {
        return repositorioOrdenCompra.findById(id)
                .filter(o -> !o.isEliminado())
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public OrdenCompra buscarAccesibleParaUsuario(String id, String email, boolean administrativo) throws Exception {
        return buscarAccesible(id, email, administrativo);
    }

    @Transactional
    public OrdenCompra crearOrdenCompra(String email) throws Exception {
        Usuario usuario = usuarioServicio.buscarActivoPorNombreUsuario(email);
        if (usuario.getRolUsuario() != com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario.CLIENTE) {
            throw new AccessDeniedException("Solo un cliente puede crear una orden de compra");
        }

        OrdenCompra orden = new OrdenCompra();
        orden.setIdentificadorCompra("ORD-" + UUID.randomUUID());
        orden.setFecha(new Date());
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        orden.setTotal(0.0);
        orden.setEliminado(false);
        orden.setUsuario(usuario);

        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra agregarDetalleAOrden(String ordenId, String productoId, int cantidad, double precioUnitario,
                                             String email, boolean administrativo) throws Exception {
        OrdenCompra orden = buscarAccesible(ordenId, email, administrativo);
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException("Solo se pueden editar órdenes pendientes de pago");
        }
        if (cantidad <= 0 || precioUnitario <= 0) {
            throw new IllegalArgumentException("La cantidad y el precio deben ser mayores a cero");
        }
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
        if (!transicionPermitida(orden.getEstadoOrdenCompra(), nuevoEstado)) {
            throw new IllegalStateException("La transición de estado solicitada no está permitida");
        }
        orden.setEstadoOrdenCompra(nuevoEstado);
        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public void anularOrdenDeUsuario(String id, String email) throws Exception {
        OrdenCompra orden = buscarAccesible(id, email, false);
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException("Solo se puede anular una orden pendiente de pago");
        }
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.ANULADA);
        repositorioOrdenCompra.save(orden);
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

    private OrdenCompra buscarAccesible(String id, String email, boolean administrativo) throws Exception {
        OrdenCompra orden = buscarPorId(id);
        if (!administrativo && (orden.getUsuario() == null
                || !orden.getUsuario().getNombreUsuario().equals(email))) {
            throw new AccessDeniedException("No tenés permiso para acceder a esta orden");
        }
        return orden;
    }

    private boolean transicionPermitida(EstadoOrdenCompra actual, EstadoOrdenCompra nueva) {
        return switch (actual) {
            case PENDIENTE_DE_PAGO -> nueva == EstadoOrdenCompra.PAGO_REALIZADO || nueva == EstadoOrdenCompra.ANULADA;
            case PAGO_REALIZADO -> nueva == EstadoOrdenCompra.PENDIENTE_DE_ENTREGA;
            case PENDIENTE_DE_ENTREGA -> nueva == EstadoOrdenCompra.PENDIENTE_DE_ENVIO;
            case PENDIENTE_DE_ENVIO -> nueva == EstadoOrdenCompra.ENTREGADO;
            default -> false;
        };
    }
}
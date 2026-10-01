package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompra;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private ServicioVigenciaPrecio servicioVigenciaPrecio;

    @Autowired
    private ServicioStock servicioStock;
    @Autowired
    private ServicioSucursal servicioSucursal;

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

    @Transactional
    public OrdenCompra obtenerCarrito(String email) throws Exception {
        Usuario usuario = usuarioServicio.buscarActivoPorNombreUsuario(email);
        if (usuario.getRolUsuario() != com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario.CLIENTE) {
            throw new AccessDeniedException("Solo un cliente puede usar el carrito");
        }
        OrdenCompra carrito = repositorioOrdenCompra
                .findFirstByUsuario_IdAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                        usuario.getId(), EstadoOrdenCompra.PENDIENTE_DE_PAGO)
                .orElseGet(() -> {
                    OrdenCompra nueva = new OrdenCompra();
                    nueva.setIdentificadorCompra("ORD-" + UUID.randomUUID());
                    nueva.setFecha(new Date());
                    nueva.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_DE_PAGO);
                    nueva.setTotal(0.0);
                    nueva.setEliminado(false);
                    nueva.setUsuario(usuario);
                    nueva.setSucursal(servicioSucursal.obtenerPrincipal());
                    return repositorioOrdenCompra.save(nueva);
                });
        if (carrito.getSucursal() == null) {
            carrito.setSucursal(servicioSucursal.obtenerPrincipal());
            repositorioOrdenCompra.save(carrito);
        }
        if (actualizarPreciosVigentes(carrito)) {
            recalcularTotal(carrito);
            repositorioOrdenCompra.save(carrito);
        }
        return carrito;
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
        orden.setSucursal(servicioSucursal.obtenerPrincipal());

        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra agregarDetalleAOrden(String ordenId, String productoId, int cantidad,
                                             String email, boolean administrativo) throws Exception {
        OrdenCompra orden = buscarAccesible(ordenId, email, administrativo);
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_DE_PAGO
                || orden.getMercadoPagoPreferenceId() != null) {
            throw new IllegalStateException("Solo se pueden editar órdenes pendientes de pago");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        Producto producto = servicioProducto.buscarPorId(productoId); // Delega la búsqueda al servicio de Producto
        if (producto.isEliminado()) {
            throw new IllegalArgumentException("El producto ya no está disponible");
        }

        var vigencia = servicioVigenciaPrecio.buscarVigenciaPrecioVigente(productoId);
        if (vigencia == null || vigencia.isEliminado() || vigencia.getFechaDesde().isAfter(java.time.LocalDate.now())) {
            throw new IllegalStateException("El producto no tiene un precio vigente");
        }

        int stockDisponible = servicioStock.cantidadActual(productoId, orden.getSucursal().getId());
        int cantidadEnCarrito = orden.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado() && detalle.getProducto().getId().equals(productoId))
                .mapToInt(DetalleCompra::getCantidad)
                .sum();
        if (cantidadEnCarrito + cantidad > stockDisponible) {
            throw new IllegalStateException("No hay stock suficiente para esa cantidad");
        }

        double subtotal = BigDecimal.valueOf(vigencia.getPrecio())
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(subtotal);
        detalle.setOrdenCompra(orden);
        detalle.setEliminado(false);

        orden.getDetalles().add(detalle);
        recalcularTotal(orden);

        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra agregarAlCarrito(String productoId, int cantidad, String email) throws Exception {
        OrdenCompra carrito = obtenerCarrito(email);
        return agregarDetalleAOrden(carrito.getId(), productoId, cantidad, email, false);
    }

    @Transactional
    public OrdenCompra eliminarDelCarrito(String detalleId, String email) throws Exception {
        OrdenCompra carrito = obtenerCarrito(email);
        if (carrito.getMercadoPagoPreferenceId() != null) {
            throw new IllegalStateException("No se puede modificar el carrito luego de iniciar el pago");
        }
        DetalleCompra detalle = carrito.getDetalles().stream()
                .filter(actual -> actual.getId().equals(detalleId) && !actual.isEliminado())
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("El producto no pertenece a tu carrito"));
        detalle.setEliminado(true);
        recalcularTotal(carrito);
        return repositorioOrdenCompra.save(carrito);
    }

    @Transactional(readOnly = true)
    public OrdenCompra validarCarritoParaCheckout(String id, String email) throws Exception {
        OrdenCompra orden = buscarAccesible(id, email, false);
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException("La orden no está pendiente de pago");
        }
        List<DetalleCompra> detalles = orden.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado())
                .toList();
        if (detalles.isEmpty()) {
            throw new IllegalStateException("Agregá al menos un producto antes de pagar");
        }
        for (DetalleCompra detalle : detalles) {
            var precioVigente = servicioVigenciaPrecio.buscarVigenciaPrecioVigente(detalle.getProducto().getId());
            if (precioVigente == null || precioVigente.isEliminado()
                || precioVigente.getFechaDesde().isAfter(java.time.LocalDate.now())) {
            throw new IllegalStateException("El producto " + detalle.getProducto().getNombre()
                + " ya no tiene un precio vigente");
            }
            BigDecimal subtotalActual = BigDecimal.valueOf(precioVigente.getPrecio())
                .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                .setScale(2, RoundingMode.HALF_UP);
            if (subtotalActual.compareTo(BigDecimal.valueOf(detalle.getSubtotal())
                .setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalStateException("El precio de " + detalle.getProducto().getNombre()
                + " cambió. Revisá el carrito antes de pagar");
            }
            Sucursal sucursal = orden.getSucursal() == null
                    ? servicioSucursal.obtenerPrincipal()
                    : orden.getSucursal();
            int disponible = servicioStock.cantidadActual(detalle.getProducto().getId(), sucursal.getId());
            if (detalle.getCantidad() > disponible) {
                throw new IllegalStateException("El stock de " + detalle.getProducto().getNombre()
                        + " cambió. Actualizá el carrito antes de pagar");
            }
        }
        return orden;
    }

    @Transactional
    public OrdenCompra registrarPreferenciaMercadoPago(String id, String email,
            String preferenceId, String initPoint) throws Exception {
        OrdenCompra orden = validarCarritoParaCheckout(id, email);
        if (orden.getMercadoPagoPreferenceId() != null) {
            throw new IllegalStateException("La orden ya tiene un pago iniciado");
        }
        orden.setMercadoPagoPreferenceId(preferenceId);
        orden.setMercadoPagoInitPoint(initPoint);
        orden.setMercadoPagoStatus("preference_created");
        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra registrarResultadoMercadoPago(String id, String paymentId, String paymentStatus)
            throws Exception {
        OrdenCompra orden = buscarPorId(id);
        if (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PAGO_REALIZADO) {
            if (paymentId.equals(orden.getMercadoPagoPaymentId())) {
                return orden;
            }
            throw new IllegalStateException("La orden ya fue pagada con otra transacción");
        }
        if (orden.getEstadoOrdenCompra() != EstadoOrdenCompra.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException("La orden ya no puede recibir un pago");
        }
        orden.setMercadoPagoPaymentId(paymentId);
        orden.setMercadoPagoStatus(paymentStatus);
        if ("approved".equalsIgnoreCase(paymentStatus)) {
            servicioStock.registrarVenta(orden);
            orden.setEstadoOrdenCompra(EstadoOrdenCompra.PAGO_REALIZADO);
        }
        return repositorioOrdenCompra.save(orden);
    }

    @Transactional
    public OrdenCompra cambiarEstado(String id, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = buscarPorId(id);
        if (!transicionPermitida(orden.getEstadoOrdenCompra(), nuevoEstado)) {
            throw new IllegalStateException("La transición de estado solicitada no está permitida");
        }
        if (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PENDIENTE_DE_PAGO
                && nuevoEstado == EstadoOrdenCompra.PAGO_REALIZADO) {
            servicioStock.registrarVenta(orden);
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
        if ("pending".equalsIgnoreCase(orden.getMercadoPagoStatus())
                || "in_process".equalsIgnoreCase(orden.getMercadoPagoStatus())
                || "preference_created".equalsIgnoreCase(orden.getMercadoPagoStatus())) {
            throw new IllegalStateException("No se puede anular una orden mientras Mercado Pago procesa el pago");
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

    private boolean actualizarPreciosVigentes(OrdenCompra carrito) {
        if (carrito.getMercadoPagoPreferenceId() != null) {
            return false;
        }
        boolean actualizado = false;
        for (DetalleCompra detalle : carrito.getDetalles()) {
            if (detalle.isEliminado()) {
                continue;
            }
            var vigencia = servicioVigenciaPrecio.buscarVigenciaPrecioVigente(detalle.getProducto().getId());
            if (vigencia == null || vigencia.isEliminado()
                    || vigencia.getFechaDesde().isAfter(java.time.LocalDate.now())) {
                continue;
            }
            double subtotal = BigDecimal.valueOf(vigencia.getPrecio())
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            if (Double.compare(detalle.getSubtotal(), subtotal) != 0) {
                detalle.setSubtotal(subtotal);
                actualizado = true;
            }
        }
        return actualizado;
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

    @Transactional
    public void asociarFacturaCliente(String ordenId, FacturaCliente factura) {
        OrdenCompra orden = buscarPorId(ordenId);
        orden.setFacturaCliente(factura);
        repositorioOrdenCompra.save(orden);
    }
}
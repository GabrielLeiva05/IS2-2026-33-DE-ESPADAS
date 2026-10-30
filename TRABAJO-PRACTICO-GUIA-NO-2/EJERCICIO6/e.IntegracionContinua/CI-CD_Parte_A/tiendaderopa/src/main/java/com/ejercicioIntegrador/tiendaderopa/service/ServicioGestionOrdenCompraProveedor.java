package com.ejercicioIntegrador.tiendaderopa.service;


import com.ejercicioIntegrador.tiendaderopa.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.List;

/**
 * Pure Fabrication: no representa ninguna entidad del negocio, existe
 * para que ControladorOrdenCompraProveedor tenga UN solo Service al
 * que hablarle. Este Service es el único punto que conoce a los otros
 * seis — ninguno de ellos conoce a este, así que no hay ciclo.
 */
@Service
public class ServicioGestionOrdenCompraProveedor {

    private static final String RUTA = "/admin/ordenes-compra-proveedor";

    @Autowired private ServicioOrdenCompraProveedor svcOrden;
    @Autowired private ServicioFacturaProveedor svcFacturaProveedor;
    @Autowired private ServicioFactura svcFactura;
    @Autowired private ServicioProveedor svcProveedor;
    @Autowired private ServicioProducto svcProducto;
    @Autowired private ServicioFormaDePago svcFormaDePago;

    public void cargarListado(Model model) {
        AdminPageSupport.cargar(model, "Órdenes de compra a proveedores", RUTA,
                OrdenCompraProveedor.class, svcOrden.listarOrdenCompraProveedor(), List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        model.addAttribute("permitirVerDetalle", true);
        model.addAttribute("permitirCambiarEstado", true);
        model.addAttribute("permitirIrANuevo", true);
        model.addAttribute("estadosOrden", List.of(EstadoOrdenCompraProveedor.values()));
    }

    public void cargarFormularioNuevo(Model model) {
        AdminPageSupport.cargar(model, "Nueva orden de compra a proveedor", RUTA,
                OrdenCompraProveedor.class, List.of(),
                List.of(AdminPageSupport.campoRelacion("idProveedor", true,
                        AdminPageSupport.mapaOpciones(svcProveedor.listarProveedorActivo(),
                                Proveedor::getId, Proveedor::getRazonSocial))),
                null);
        model.addAttribute("permitirCrear", true);
        model.addAttribute("accion", RUTA + "/iniciar");
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
    }

    @Transactional
    public String iniciarOrden(String idProveedor) throws Exception {
        return svcOrden.iniciarOrden(idProveedor).getId();
    }

    public void cargarDetalle(Model model, String id) throws Exception {
        OrdenCompraProveedor orden = svcOrden.buscarOrdenCompraProveedor(id);
        boolean yaConfirmada = existeFacturaPara(id);

        AdminPageSupport.cargar(model, "Orden a " + orden.getProveedor().getRazonSocial(), RUTA,
                DetalleOrdenCompraProveedor.class, svcOrden.listarDetallePorOrden(id), List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        model.addAttribute("ordenId", id);
        model.addAttribute("permitirAgregarDetalle", !yaConfirmada);
        model.addAttribute("permitirPrecioCompra", true);
        model.addAttribute("permitirConfirmarOrden", !yaConfirmada);
        model.addAttribute("productosDisponibles", AdminPageSupport.mapaOpciones(
                svcProducto.listarProductoActivo(), Producto::getId, Producto::getNombre));
        model.addAttribute("formasDePagoDisponibles", AdminPageSupport.mapaOpciones(
                svcFormaDePago.listarFormaDePagoActivo(), FormaDePago::getId, fp -> fp.getTipoPago().toString()));
    }

    @Transactional
    public void agregarDetalle(String idOrden, String idProducto, int cantidad, double precioCompra) throws Exception {
        svcOrden.agregarDetalle(idOrden, idProducto, cantidad, precioCompra);
    }

    @Transactional
    public void confirmarOrden(String id, String idFormaDePago) throws Exception {
        OrdenCompraProveedor orden = svcOrden.buscarOrdenCompraProveedor(id);
        List<DetalleOrdenCompraProveedor> detalles = svcOrden.listarDetallePorOrden(id);
        if (detalles.isEmpty()) {
            throw new Exception("Agregá al menos un producto antes de aceptar la orden");
        }
        Long numeroFactura = svcFactura.generarNumeroFacturaUnico();
        FacturaProveedor factura = svcFacturaProveedor.crearFactura(numeroFactura, new Date(),
                orden.getTotal(), idFormaDePago, orden.getProveedor().getId(), orden.getId());

        for (DetalleOrdenCompraProveedor detalle : detalles) {
            svcFactura.crearDetalleFactura(factura, detalle.getProducto(),
                    detalle.getCantidad(), detalle.getCantidad() * detalle.getPrecioCompra());
        }
    }

    @Transactional
    public void cambiarEstado(String id, EstadoOrdenCompraProveedor estado) throws Exception {
        if (estado == EstadoOrdenCompraProveedor.ENTREGADA) {
            svcOrden.marcarComoEntregada(id); // ya dispara registrarRecepcion (stock) adentro, sin cambios
            FacturaProveedor factura = svcFacturaProveedor.buscarPorOrdenCompra(id);
            svcFactura.marcarFacturaComoPagada(factura.getId());
        } else if (estado == EstadoOrdenCompraProveedor.ANULADA) {
            FacturaProveedor factura = svcFacturaProveedor.buscarPorOrdenCompra(id);
            svcFactura.anularFactura(factura.getId());
            svcOrden.anularOrdenCompraProveedor(id);
        } else {
            throw new Exception("Esa transición de estado no está soportada");
        }
    }

    private boolean existeFacturaPara(String idOrden) {
        try {
            svcFacturaProveedor.buscarPorOrdenCompra(idOrden);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
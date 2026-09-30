package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("hasRole('CLIENTE')")
public class MisOrdenesController {

    private final ServicioOrdenCompra servicio;

    public MisOrdenesController(ServicioOrdenCompra servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/mis-ordenes")
    public String listar(@RequestParam(required = false) EstadoOrdenCompra estado, Authentication authentication, Model model)
            throws Exception {
        List<OrdenCompra> ordenes = servicio.listarActivasDeUsuario(authentication.getName());
        if (estado != null) {
            ordenes = ordenes.stream().filter(orden -> orden.getEstadoOrdenCompra() == estado).toList();
        }
        return mostrar(model, ordenes, estado);
    }

    @GetMapping("/mis-ordenes/estado/{estado}")
        public String listarPorEstado(@PathVariable EstadoOrdenCompra estado, Authentication authentication, Model model)
            throws Exception {
        List<OrdenCompra> ordenes = servicio.listarActivasDeUsuario(authentication.getName()).stream()
                .filter(orden -> orden.getEstadoOrdenCompra() == estado).toList();
        return mostrar(model, ordenes, estado);
    }

    @GetMapping("/mis-ordenes/{id}")
    public String ver(@PathVariable String id, Authentication authentication, Model model) throws Exception {
        OrdenCompra orden = servicio.buscarAccesibleParaUsuario(id, authentication.getName(), false);
        AdminPageSupport.cargar(model, "Detalle de orden " + orden.getIdentificadorCompra(), "/mis-ordenes",
                DetalleCompra.class,
                orden.getDetalles().stream().filter(detalle -> !detalle.isEliminado()).toList(), List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        model.addAttribute("permitirAgregarDetalle", true);
        model.addAttribute("ordenId", id);
        return "admin/registros";
    }

    @PostMapping("/mis-ordenes/crear")
    public String crear(Authentication authentication) throws Exception {
        servicio.obtenerCarrito(authentication.getName());
        return "redirect:/carrito";
    }

    @PostMapping("/mis-ordenes/{ordenId}/detalles")
    public String agregarDetalle(@PathVariable String ordenId, @RequestParam String productoId,
            @RequestParam int cantidad, Authentication authentication, RedirectAttributes redirect) {
        try {
            servicio.agregarDetalleAOrden(ordenId, productoId, cantidad, authentication.getName(), false);
            redirect.addFlashAttribute("mensaje", "Producto agregado a la orden.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mis-ordenes/" + ordenId;
    }

    @PostMapping("/mis-ordenes/{id}/anular")
    public String anular(@PathVariable String id, Authentication authentication, RedirectAttributes redirect) {
        try {
            servicio.anularOrdenDeUsuario(id, authentication.getName());
            redirect.addFlashAttribute("mensaje", "Orden anulada correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mis-ordenes";
    }

    private String mostrar(Model model, List<OrdenCompra> ordenes, EstadoOrdenCompra estado) {
        AdminPageSupport.cargar(model, "Mis órdenes", "/mis-ordenes", OrdenCompra.class, ordenes, List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        model.addAttribute("permitirVerDetalle", true);
        model.addAttribute("permitirAnular", true);
        model.addAttribute("permitirFiltrarEstado", true);
        model.addAttribute("permitirCrearOrden", true);
        model.addAttribute("estadoSeleccionado", estado == null ? "" : estado.name());
        model.addAttribute("estadosOrden", List.of(EstadoOrdenCompra.values()));
        return "admin/registros";
    }
}
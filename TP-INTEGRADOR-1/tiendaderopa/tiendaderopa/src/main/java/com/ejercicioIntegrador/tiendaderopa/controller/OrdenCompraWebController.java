package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
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
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class OrdenCompraWebController {

    private static final String RUTA = "/admin/ordenes-compra";
    private static final List<EstadoOrdenCompra> ESTADOS_DISPONIBLES = List.of(
            EstadoOrdenCompra.PENDIENTE_DE_PAGO,
            EstadoOrdenCompra.PAGO_REALIZADO,
            EstadoOrdenCompra.PENDIENTE_DE_ENTREGA,
            EstadoOrdenCompra.PENDIENTE_DE_ENVIO,
            EstadoOrdenCompra.ENTREGADO,
            EstadoOrdenCompra.ANULADA);

    private final ServicioOrdenCompra servicio;

    public OrdenCompraWebController(ServicioOrdenCompra servicio) {
        this.servicio = servicio;
    }

    @GetMapping(RUTA)
    public String listar(@RequestParam(required = false) EstadoOrdenCompra estado, Model model) {
        return mostrar(model, estado == null ? servicio.listarActivas() : servicio.listarPorEstado(estado), estado);
    }

    @GetMapping(RUTA + "/estado/{estado}")
    public String listarPorEstado(@PathVariable EstadoOrdenCompra estado, Model model) {
        return mostrar(model, servicio.listarPorEstado(estado), estado);
    }

    @GetMapping(RUTA + "/{id}")
    public String ver(@PathVariable String id, Authentication authentication, Model model) throws Exception {
        OrdenCompra orden = servicio.buscarAccesibleParaUsuario(id, authentication.getName(), true);
        AdminPageSupport.cargar(model, "Detalle de orden " + orden.getIdentificadorCompra(), RUTA,
                com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra.class,
                orden.getDetalles().stream().filter(detalle -> !detalle.isEliminado()).toList(), List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        return "admin/registros";
    }

    @PostMapping(RUTA + "/{id}/estado")
    public String cambiarEstado(@PathVariable String id, @RequestParam EstadoOrdenCompra estado,
            RedirectAttributes redirect) {
        try {
            servicio.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado de la orden actualizado.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + RUTA;
    }

    private String mostrar(Model model, List<OrdenCompra> ordenes, EstadoOrdenCompra estado) {
        AdminPageSupport.cargar(model, "Órdenes de compra", RUTA, OrdenCompra.class, ordenes, List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        model.addAttribute("permitirVerDetalle", true);
        model.addAttribute("permitirCambiarEstado", true);
        model.addAttribute("permitirFiltrarEstado", true);
        model.addAttribute("estadoSeleccionado", estado == null ? "" : estado.name());
        model.addAttribute("estadosOrden", ESTADOS_DISPONIBLES);
        return "admin/registros";
    }
}
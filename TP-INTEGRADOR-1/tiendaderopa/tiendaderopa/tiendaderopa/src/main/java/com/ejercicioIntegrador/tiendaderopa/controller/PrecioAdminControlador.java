package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProducto;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioVigenciaPrecio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/precios")
public class PrecioAdminControlador {

    private final ServicioVigenciaPrecio servicio;
    private final ServicioProducto productoServicio;

    public PrecioAdminControlador(ServicioVigenciaPrecio servicio, ServicioProducto productoServicio) {
        this.servicio = servicio;
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarVigenciaPrecio(id));
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/precios";
        }
    }

    @PostMapping
    public String crear(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam double precio, @RequestParam String idProducto, RedirectAttributes redirect) {
        try {
            servicio.crearVigenciaPrecio(fechaDesde, fechaHasta, precio, idProducto);
            redirect.addFlashAttribute("mensaje", "Precio creado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/precios";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam double precio, @RequestParam String idProducto, RedirectAttributes redirect) {
        try {
            servicio.modificarVigenciaPrecio(id, fechaDesde, fechaHasta, precio, idProducto);
            redirect.addFlashAttribute("mensaje", "Precio actualizado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/precios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarVigenciaPrecio(id);
            redirect.addFlashAttribute("mensaje", "Precio eliminado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/precios";
    }

    private String mostrar(Model model, VigenciaPrecio seleccionada) {
        AdminPageSupport.cargar(model, "Precios", "/admin/precios", VigenciaPrecio.class,
                servicio.listarVigenciaPrecio(), java.util.List.of(
                        AdminPageSupport.campo("fechaDesde", "date", true),
                        AdminPageSupport.campo("fechaHasta", "date", false),
                        AdminPageSupport.campo("precio", "number", true),
                        AdminPageSupport.campoRelacion("idProducto", true, AdminPageSupport.mapaOpciones(
                                productoServicio.listarProductoActivo(), Producto::getId, Producto::getNombre))),
                seleccionada);
        return "admin/registros";
    }
}

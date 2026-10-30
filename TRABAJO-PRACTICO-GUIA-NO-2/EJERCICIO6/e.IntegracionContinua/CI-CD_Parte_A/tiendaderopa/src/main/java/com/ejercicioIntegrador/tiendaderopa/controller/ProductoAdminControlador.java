package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Imagen;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioImagen;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProducto;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioSubCategoria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
public class ProductoAdminControlador {

    private final ServicioProducto servicio;
    private final ServicioSubCategoria subCategoriaServicio;
    private final ServicioImagen imagenServicio;

    public ProductoAdminControlador(ServicioProducto servicio, ServicioSubCategoria subCategoriaServicio,
            ServicioImagen imagenServicio) {
        this.servicio = servicio;
        this.subCategoriaServicio = subCategoriaServicio;
        this.imagenServicio = imagenServicio;
    }

    @GetMapping
    public String listar(Model model) throws Exception {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) throws Exception {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) throws Exception {
        try {
            return mostrar(model, servicio.buscarPorId(id));
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se encontró el producto solicitado.");
            return "redirect:/admin/productos";
        }
    }

    @PostMapping
    public String crear(@RequestParam String codigo, @RequestParam String nombre,
            @RequestParam(required = false) String descripcion, @RequestParam(required = false) String talle,
            @RequestParam(defaultValue = "false") boolean enOferta, @RequestParam(required = false) String idImagen,
            @RequestParam String idSubCategoria, RedirectAttributes redirect) {
        try {
            servicio.crearProducto(codigo, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
            redirect.addFlashAttribute("mensaje", "Producto creado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre,
            @RequestParam(required = false) String descripcion, @RequestParam(required = false) String talle,
            @RequestParam(defaultValue = "false") boolean enOferta, @RequestParam(required = false) String idImagen,
            @RequestParam String idSubCategoria, RedirectAttributes redirect) {
        try {
            servicio.modificarProducto(id, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
            redirect.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarProducto(id);
            redirect.addFlashAttribute("mensaje", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/restaurar")
    public String restaurar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.restaurarProducto(id);
            redirect.addFlashAttribute("mensaje", "Producto recuperado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    private String mostrar(Model model, Producto seleccionado) throws Exception {
        AdminPageSupport.cargar(model, "Productos", "/admin/productos", Producto.class,
                servicio.listarProducto(), java.util.List.of(
                        AdminPageSupport.campo("codigo", "text", true),
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campo("descripcion", "text", false),
                        AdminPageSupport.campo("talle", "text", false),
                        AdminPageSupport.campo("enOferta", "checkbox", false),
                        AdminPageSupport.campoRelacion("idSubCategoria", true, AdminPageSupport.mapaOpciones(
                                subCategoriaServicio.findAll(), SubCategoria::getId, SubCategoria::getNombre)),
                        AdminPageSupport.campoRelacion("idImagen", false, AdminPageSupport.mapaOpciones(
                                imagenServicio.listar(), Imagen::getId, Imagen::getNombre))),
                seleccionado);
            model.addAttribute("permitirRestaurar", true);
        return "admin/registros";
    }
}

package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.*;
import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;

/**
 * ABM de productos embebido en el dashboard (fragmento fragments/abmProducto.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/productos) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/producto")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ProductoAbmControlador {

    private final ServicioProducto servicioProducto;
    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioImagen servicioImagen;

    public ProductoAbmControlador(ServicioProducto servicioProducto, ServicioSubCategoria servicioSubCategoria, ServicioImagen servicioImagen) {
        this.servicioProducto = servicioProducto;
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioImagen = servicioImagen;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String codigo,
                           @RequestParam String nombre,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam(required = false) String talle,
                           @RequestParam String idSubCategoria,
                           @RequestParam(defaultValue = "false") boolean enOferta,
                           @RequestParam(required = false) MultipartFile archivo,
                           RedirectAttributes redirectAttributes) {
        try {
            // Se valida antes de guardar la imagen para no dejar imágenes huérfanas si los datos son inválidos.
            servicioProducto.validarProducto(codigo, nombre, descripcion, talle, enOferta, null, idSubCategoria);
            Imagen imagen = servicioImagen.guardar(archivo, TipoImagen.PRODUCTO);
            String idImagen = imagen != null ? imagen.getId() : null;
            servicioProducto.crearProducto(codigo, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
            redirectAttributes.addFlashAttribute("exito", "Producto cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = servicioProducto.buscarPorId(id);
            if (producto == null) {
                throw new Exception("No se encontró el producto solicitado.");
            }
            modelo.addAttribute("producto", producto);
            modelo.addAttribute("productos", servicioProducto.listarProducto());
            modelo.addAttribute("subcategorias", servicioSubCategoria.findAll());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String nombre,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam(required = false) String talle,
                           @RequestParam String idSubCategoria,
                           @RequestParam(defaultValue = "false") boolean enOferta,
                           @RequestParam(required = false) MultipartFile archivo,
                           RedirectAttributes redirectAttributes) {
        try {
            Imagen imagen = servicioImagen.guardar(archivo, TipoImagen.PRODUCTO);
            // Si no se sube una imagen nueva, idImagen queda en null y el producto conserva la que tenía.
            String idImagen = imagen != null ? imagen.getId() : null;
            servicioProducto.modificarProducto(id, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);
            redirectAttributes.addFlashAttribute("exito", "Producto modificado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioProducto.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("exito", "Producto eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}

package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Imagen;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioImagen;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ControladorImagen {

    private final ServicioImagen imagenServicio;

    public ControladorImagen(ServicioImagen imagenServicio) {
        this.imagenServicio = imagenServicio;
    }

    @GetMapping("/admin/imagenes")
    public String listar(Model model) {
        AdminPageSupport.cargar(model, "Imágenes", "/admin/imagenes", Imagen.class,
                imagenServicio.listar(), java.util.List.of(
                AdminPageSupport.campo("archivo", "file", true),
                        AdminPageSupport.campo("tipoImagen", "select", true,
                                java.util.Arrays.stream(TipoImagen.values()).map(Enum::name).toArray(String[]::new))), null);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        return "admin/registros";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String id) {


        Imagen imagen = imagenServicio.findById(id);

        if (imagen == null || imagen.getContenido() == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(imagen.getMime())
                )
                .body(imagen.getContenido());
    }

    @PostMapping("/admin/imagenes")
    public String crearImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoImagen") TipoImagen tipoImagen,
            RedirectAttributes redirect
    ) {
        try {
            imagenServicio.guardar(archivo, tipoImagen);
            redirect.addFlashAttribute("mensaje", "Imagen guardada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/imagenes";
    }

    @GetMapping("/admin/imagenes/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        Imagen imagen = imagenServicio.findById(id);
        if (imagen == null) {
            redirect.addFlashAttribute("error", "No se encontró la imagen.");
            return "redirect:/admin/imagenes";
        }
        AdminPageSupport.cargar(model, "Imágenes", "/admin/imagenes", Imagen.class,
                imagenServicio.listar(), java.util.List.of(
                AdminPageSupport.campo("archivo", "file", true),
                        AdminPageSupport.campo("tipoImagen", "select", true,
                                java.util.Arrays.stream(TipoImagen.values()).map(Enum::name).toArray(String[]::new))), imagen);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirEliminar", false);
        return "admin/registros";
    }

    @PostMapping("/admin/imagenes/{id}")
    public String modificarImagen(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoImagen") TipoImagen tipoImagen,
            RedirectAttributes redirect
    ) {
        try {
            imagenServicio.actualizar(archivo, id, tipoImagen);
            redirect.addFlashAttribute("mensaje", "Imagen actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/imagenes";
    }
}
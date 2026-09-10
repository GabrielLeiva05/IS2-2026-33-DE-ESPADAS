package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ControladorProducto {

    @Autowired
    private ServicioProducto svcProducto;

    @GetMapping("/productos")
    public String listaProductos(Model model) {
        try {
            model.addAttribute("productos", svcProducto.listarProductoActivo());
            return "views/productos/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/producto/{id}")
    public String guardarProducto(
            @ModelAttribute("producto") Producto producto,
            Model model, @PathVariable("id") String id,
            @RequestParam(required = false) String idImagen,
            @RequestParam(required = false) String idSubCategoria
    ) {
        try {
            if (id.isEmpty()) {
                svcProducto.crearProducto(producto.getCodigo(), producto.getNombre(), producto.getDescripcion(),
                        producto.getTalle(), producto.isEnOferta(), idImagen, idSubCategoria);
            } else {
                svcProducto.modificarProducto(id, producto.getNombre(), producto.getDescripcion(),
                        producto.getTalle(), producto.isEnOferta(), idImagen, idSubCategoria);
            }
            return "redirect:/productos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/producto/{id}")
    public String eliminarProducto(Model model, @PathVariable("id") String id) {
        try {
            svcProducto.eliminarProducto(id);
            return "redirect:/productos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}

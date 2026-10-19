package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.ArticuloService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Pagina principal: catalogo publico de articulos (no requiere login para mirar). */
@Controller
public class InicioController {

    private final ArticuloService articuloService;

    public InicioController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        try {
            model.addAttribute("articulos", articuloService.listarParaCatalogo());
        } catch (ErrorServiceException e) {
            model.addAttribute("articulos", List.of());
            model.addAttribute("msgError", e.getMessage());
        }
        return "view/inicio";
    }
}

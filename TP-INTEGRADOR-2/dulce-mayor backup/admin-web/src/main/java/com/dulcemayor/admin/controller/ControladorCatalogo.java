package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioCatalogo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Consulta del catálogo con precio vigente y stock disponible (administración y preventistas). */
@Controller
@RequestMapping("/catalogo")
public class ControladorCatalogo {

    private final ServicioCatalogo servicioCatalogo;
    private final GestorSesion gestorSesion;

    public ControladorCatalogo(ServicioCatalogo servicioCatalogo, GestorSesion gestorSesion) {
        this.servicioCatalogo = servicioCatalogo;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String categoria,
                         @RequestParam(required = false) String texto,
                         @RequestParam(defaultValue = "false") boolean ofertas,
                         HttpServletRequest request, Model model) {
        String token = gestorSesion.actual(request).orElseThrow().getToken();
        model.addAttribute("productos", servicioCatalogo.listarProductos(token, categoria, texto, ofertas));
        model.addAttribute("categorias", servicioCatalogo.listarCategorias(token));
        model.addAttribute("categoria", categoria);
        model.addAttribute("texto", texto);
        model.addAttribute("ofertas", ofertas);
        return "catalogo";
    }
}

package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.services.ServicioCategoria;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class controladorCategoria {

    @Autowired
    private ServicioCategoria svcCategoria;

    @GetMapping("/categorias")
    public String listaCategorias(Model model) {
        try {
            model.addAttribute("categorias", this.svcCategoria.findAll());
            return "views/categorias/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/categoria/{id}")
    public String formularioCategoria(Model model, @PathVariable("id") long id) {
        try {
            if (id == 0) {
                model.addAttribute("categoria", new Categoria());
            } else {
                model.addAttribute("categoria", this.svcCategoria.findById(id));
            }
            return "views/categorias/formulario";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/categoria/{id}")
    public String guardarCategoria(
            @Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult result,
            Model model, @PathVariable("id") long id
    ) {
        try {
            if (result.hasErrors()) {
                return "views/categorias/formulario";
            }
            if (id == 0) {
                this.svcCategoria.saveOne(categoria);
            } else {
                this.svcCategoria.updateOne(categoria, id);
            }
            return "redirect:/categorias";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/eliminar/categoria/{id}")
    public String eliminarCategoria(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("categoria", this.svcCategoria.findById(id));
            return "views/categorias/eliminar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/categoria/{id}")
    public String desactivarCategoria(Model model, @PathVariable("id") long id) {
        try {
            this.svcCategoria.deleteById(id);
            return "redirect:/categorias";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}

package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioCategoria;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioSubCategoria;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ControladorSubCategoria {

    @Autowired
    private ServicioSubCategoria svcSubCategoria;

    @GetMapping("/subcategorias")
    public String listaSubCategorias(Model model) {
        try {
            model.addAttribute("subcategorias", this.svcSubCategoria.findAll());
            return "views/subcategorias/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/subcategoria/{id}")
    public String formularioSubCategoria(Model model, @PathVariable("id") String id) {
        try {
            if (id == "") {
                model.addAttribute("subcategoria", new SubCategoria());
            } else {
                model.addAttribute("subcategoria", this.svcSubCategoria.findById(id));
            }
            return "views/subcategorias/formulario";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/subcategoria/{id}")
    public String guardarSubCategoria(
            @Valid @ModelAttribute("subcategoria") SubCategoria subCategoria,
            BindingResult result,
            Model model, @PathVariable("id") String id
    ) {
        try {
            if (result.hasErrors()) {
                return "views/subcategorias/formulario";
            }
            if (id == "") {
                this.svcSubCategoria.saveOne(subCategoria);
            } else {
                this.svcSubCategoria.updateOne(subCategoria, id);
            }
            return "redirect:/subcategorias";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/eliminar/subcategoria/{id}")
    public String eliminarSubCategoria(Model model, @PathVariable("id") String id) {
        try {
            model.addAttribute("subcategoria", this.svcSubCategoria.findById(id));
            return "views/subcategorias/eliminar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/subcategoria/{id}")
    public String desactivarSubCategoria(Model model, @PathVariable("id") String id) {
        try {
            this.svcSubCategoria.deleteById(id);
            return "redirect:/subcategorias";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }


}

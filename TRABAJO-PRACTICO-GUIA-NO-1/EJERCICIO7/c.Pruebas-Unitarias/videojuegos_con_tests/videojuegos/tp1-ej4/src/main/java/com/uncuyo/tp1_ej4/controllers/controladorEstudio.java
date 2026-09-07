package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.services.ServicioEstudio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class controladorEstudio {

    @Autowired
    private ServicioEstudio svcEstudio;

    @GetMapping("/estudios")
    public String listaEstudios(Model model) {
        try {
            model.addAttribute("estudios", this.svcEstudio.findAll());
            return "views/estudios/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/estudio/{id}")
    public String formularioEstudio(Model model, @PathVariable("id") long id) {
        try {
            if (id == 0) {
                model.addAttribute("estudio", new Estudio());
            } else {
                model.addAttribute("estudio", this.svcEstudio.findById(id));
            }
            return "views/estudios/formulario";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/estudio/{id}")
    public String guardarEstudio(
            @Valid @ModelAttribute("estudio") Estudio estudio,
            BindingResult result,
            Model model, @PathVariable("id") long id
    ) {
        try {
            if (result.hasErrors()) {
                return "views/estudios/formulario";
            }
            if (id == 0) {
                this.svcEstudio.saveOne(estudio);
            } else {
                this.svcEstudio.updateOne(estudio, id);
            }
            return "redirect:/estudios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/eliminar/estudio/{id}")
    public String eliminarEstudio(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("estudio", this.svcEstudio.findById(id));
            return "views/estudios/eliminar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/estudio/{id}")
    public String desactivarEstudio(Model model, @PathVariable("id") long id) {
        try {
            this.svcEstudio.deleteById(id);
            return "redirect:/estudios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}

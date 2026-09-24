package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.LocalidadDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.LocalidadService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/localidades")
public class LocalidadController {

    private static final String FORMULARIO = "localidades/formulario";

    private final LocalidadService service;

    public LocalidadController(LocalidadService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("localidades", service.listar());
        return "localidades/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("localidad", new LocalidadDTO());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("localidad") LocalidadDTO localidad, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return FORMULARIO;
        }
        try {
            service.crear(localidad);
            flash.addFlashAttribute("mensaje", "La localidad se creó correctamente.");
            return "redirect:/localidades";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("localidad", service.obtener(id));
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("localidad") LocalidadDTO localidad, BindingResult result,
                             Model model, RedirectAttributes flash) {
        localidad.setId(id);
        if (result.hasErrors()) {
            return FORMULARIO;
        }
        try {
            service.actualizar(id, localidad);
            flash.addFlashAttribute("mensaje", "La localidad se guardó correctamente.");
            return "redirect:/localidades";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            service.eliminar(id);
            flash.addFlashAttribute("mensaje", "La localidad se eliminó.");
        } catch (ApiException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/localidades";
    }
}

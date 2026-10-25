package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.AutorDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.AutorService;
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
@RequestMapping("/autores")
public class AutorController {

    private static final String FORMULARIO = "autores/formulario";

    private final AutorService service;

    public AutorController(AutorService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("autores", service.listar());
        return "autores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("autor", new AutorDTO());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("autor") AutorDTO autor, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return FORMULARIO;
        }
        try {
            service.crear(autor);
            flash.addFlashAttribute("mensaje", "El autor se creó correctamente.");
            return "redirect:/autores";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("autor", service.obtener(id));
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("autor") AutorDTO autor, BindingResult result,
                             Model model, RedirectAttributes flash) {
        autor.setId(id);
        if (result.hasErrors()) {
            return FORMULARIO;
        }
        try {
            service.actualizar(id, autor);
            flash.addFlashAttribute("mensaje", "El autor se guardó correctamente.");
            return "redirect:/autores";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            service.eliminar(id);
            flash.addFlashAttribute("mensaje", "El autor se eliminó.");
        } catch (ApiException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/autores";
    }
}

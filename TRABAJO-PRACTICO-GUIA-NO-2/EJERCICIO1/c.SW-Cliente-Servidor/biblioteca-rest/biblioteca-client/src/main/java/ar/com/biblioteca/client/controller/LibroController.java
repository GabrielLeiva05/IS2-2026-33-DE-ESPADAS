package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.LibroDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.AutorService;
import ar.com.biblioteca.client.service.PersonaService;
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

/**
 * Alta, modificación y baja de los libros de una persona (composición Persona ◆ Libro).
 * Las rutas cuelgan de la persona propietaria: /personas/{personaId}/libros/...
 */
@Controller
@RequestMapping("/personas/{personaId}/libros")
public class LibroController {

    private static final String FORMULARIO = "libros/formulario";

    private final PersonaService personaService;
    private final AutorService autorService;

    public LibroController(PersonaService personaService, AutorService autorService) {
        this.personaService = personaService;
        this.autorService = autorService;
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable("personaId") Long personaId, Model model) {
        cargarFormulario(model, personaId, new LibroDTO());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@PathVariable("personaId") Long personaId,
                        @Valid @ModelAttribute("libro") LibroDTO libro, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            cargarFormulario(model, personaId, libro);
            return FORMULARIO;
        }
        try {
            personaService.crearLibro(personaId, libro);
            flash.addFlashAttribute("mensaje", "El libro se agregó correctamente.");
            return "redirect:/personas/" + personaId;
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            cargarFormulario(model, personaId, libro);
            return FORMULARIO;
        }
    }

    @GetMapping("/{libroId}/editar")
    public String editar(@PathVariable("personaId") Long personaId,
                         @PathVariable("libroId") Long libroId, Model model) {
        cargarFormulario(model, personaId, personaService.obtenerLibro(personaId, libroId));
        return FORMULARIO;
    }

    @PostMapping("/{libroId}")
    public String actualizar(@PathVariable("personaId") Long personaId,
                             @PathVariable("libroId") Long libroId,
                             @Valid @ModelAttribute("libro") LibroDTO libro, BindingResult result,
                             Model model, RedirectAttributes flash) {
        libro.setId(libroId); // el formulario se vuelve a mostrar en modo edición si hay errores
        if (result.hasErrors()) {
            cargarFormulario(model, personaId, libro);
            return FORMULARIO;
        }
        try {
            personaService.actualizarLibro(personaId, libroId, libro);
            flash.addFlashAttribute("mensaje", "El libro se guardó correctamente.");
            return "redirect:/personas/" + personaId;
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            cargarFormulario(model, personaId, libro);
            return FORMULARIO;
        }
    }

    @PostMapping("/{libroId}/eliminar")
    public String eliminar(@PathVariable("personaId") Long personaId,
                           @PathVariable("libroId") Long libroId, RedirectAttributes flash) {
        try {
            personaService.eliminarLibro(personaId, libroId);
            flash.addFlashAttribute("mensaje", "El libro se eliminó.");
        } catch (ApiException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/personas/" + personaId;
    }

    private void cargarFormulario(Model model, Long personaId, LibroDTO libro) {
        model.addAttribute("libro", libro);
        model.addAttribute("persona", personaService.obtener(personaId));
        model.addAttribute("autores", autorService.listar());
    }
}

package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.PersonaDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.LocalidadService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    private static final String FORMULARIO = "personas/formulario";

    private final PersonaService personaService;
    private final LocalidadService localidadService;

    public PersonaController(PersonaService personaService, LocalidadService localidadService) {
        this.personaService = personaService;
        this.localidadService = localidadService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "filtro", required = false) String filtro, Model model) {
        model.addAttribute("personas", personaService.listar(filtro));
        model.addAttribute("filtro", filtro);
        return "personas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        model.addAttribute("persona", personaService.obtener(id));
        return "personas/detalle";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("persona", new PersonaDTO());
        cargarLocalidades(model);
        return FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("persona") PersonaDTO persona, BindingResult result,
                        Model model, RedirectAttributes flash) {
        validarLocalidad(persona, result);
        if (result.hasErrors()) {
            cargarLocalidades(model);
            return FORMULARIO;
        }
        try {
            PersonaDTO creada = personaService.crear(persona);
            flash.addFlashAttribute("mensaje", "La persona se creó correctamente.");
            return "redirect:/personas/" + creada.getId();
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            cargarLocalidades(model);
            return FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("persona", personaService.obtener(id));
        cargarLocalidades(model);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("persona") PersonaDTO persona, BindingResult result,
                             Model model, RedirectAttributes flash) {
        persona.setId(id); // el formulario se vuelve a mostrar en modo edición si hay errores
        validarLocalidad(persona, result);
        if (result.hasErrors()) {
            cargarLocalidades(model);
            return FORMULARIO;
        }
        try {
            personaService.actualizar(id, persona);
            flash.addFlashAttribute("mensaje", "Los datos de la persona se guardaron correctamente.");
            return "redirect:/personas/" + id;
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            cargarLocalidades(model);
            return FORMULARIO;
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            personaService.eliminar(id);
            flash.addFlashAttribute("mensaje", "La persona, su domicilio y sus libros se eliminaron.");
        } catch (ApiException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/personas";
    }

    private void cargarLocalidades(Model model) {
        model.addAttribute("localidades", localidadService.listar());
    }

    /** La localidad se elige de una lista: se exige que se haya seleccionado una. */
    private void validarLocalidad(PersonaDTO persona, BindingResult result) {
        if (persona.getDomicilio() == null || persona.getDomicilio().getLocalidad() == null
                || persona.getDomicilio().getLocalidad().getId() == null) {
            result.rejectValue("domicilio.localidad.id", "NotNull", "Seleccione una localidad");
        }
    }
}

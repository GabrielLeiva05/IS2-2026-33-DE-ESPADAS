package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.PersonaDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.AutorService;
import ar.com.biblioteca.client.service.LocalidadService;
import ar.com.biblioteca.client.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PersonaService personaService;
    private final AutorService autorService;
    private final LocalidadService localidadService;

    public HomeController(PersonaService personaService, AutorService autorService,
                          LocalidadService localidadService) {
        this.personaService = personaService;
        this.autorService = autorService;
        this.localidadService = localidadService;
    }

    /** Portada: resumen con las cantidades de cada entidad (si el servidor responde). */
    @GetMapping("/")
    public String inicio(Model model) {
        try {
            List<PersonaDTO> personas = personaService.listar(null);
            int libros = personas.stream().mapToInt(p -> p.getLibros().size()).sum();
            model.addAttribute("cantPersonas", personas.size());
            model.addAttribute("cantLibros", libros);
            model.addAttribute("cantAutores", autorService.listar().size());
            model.addAttribute("cantLocalidades", localidadService.listar().size());
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }
}

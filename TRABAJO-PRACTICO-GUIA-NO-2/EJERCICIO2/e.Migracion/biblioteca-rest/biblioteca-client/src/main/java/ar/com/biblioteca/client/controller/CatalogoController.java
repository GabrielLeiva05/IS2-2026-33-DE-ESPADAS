package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Vista de solo lectura con todos los libros del sistema y su propietario. */
@Controller
public class CatalogoController {

    private final PersonaService personaService;

    public CatalogoController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping("/libros")
    public String catalogo(Model model) {
        model.addAttribute("filas", personaService.listarTodosLosLibros());
        return "libros/catalogo";
    }
}

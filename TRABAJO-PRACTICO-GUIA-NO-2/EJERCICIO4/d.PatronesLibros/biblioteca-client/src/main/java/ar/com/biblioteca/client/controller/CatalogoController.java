package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ar.com.biblioteca.client.exception.ApiException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Vista de solo lectura con todos los libros del sistema y su propietario. */
@Controller
public class CatalogoController {

    private final PersonaService personaService;

    public CatalogoController(PersonaService personaService) {
        this.personaService = personaService;
    }

    /**
     * /libros                                → todos
     * /libros?criterio=autor&valor=borges    → búsqueda (patrón Strategy en el servidor)
     */
    @GetMapping("/libros")
    public String catalogo(@RequestParam(name = "criterio", required = false) String criterio,
                           @RequestParam(name = "valor", required = false) String valor,
                           Model model) {
        model.addAttribute("criterio", criterio == null ? "titulo" : criterio);
        model.addAttribute("valor", valor);
        try {
            model.addAttribute("filas", personaService.buscarLibros(criterio, valor));
            model.addAttribute("filtrando", criterio != null && !criterio.isBlank());
        } catch (ApiException e) {
            // Ej.: "Debe indicar un valor de búsqueda" → se avisa y se muestran todos
            model.addAttribute("error", e.getMessage());
            model.addAttribute("filas", personaService.listarTodosLosLibros());
            model.addAttribute("filtrando", false);
        }
        return "libros/catalogo";
    }
}

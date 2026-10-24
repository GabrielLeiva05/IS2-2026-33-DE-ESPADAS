package ar.com.biblioteca.client.controller;

import ar.com.biblioteca.client.dto.LibroDTO;
import ar.com.biblioteca.client.exception.ApiException;
import ar.com.biblioteca.client.service.AutorService;
import ar.com.biblioteca.client.service.PersonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

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
        validarArchivo(libro, result);
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
        validarArchivo(libro, result);
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

    /**
     * Abre el PDF del libro. El navegador lo pide al CLIENTE (nunca directamente al servidor) y el cliente lo
     * obtiene de la API con RestTemplate. Con "inline" el navegador lo muestra en su visor en vez de descargarlo;
     * los enlaces de las vistas usan target="_blank" para abrirlo en una solapa nueva.
     */
    @GetMapping("/{libroId}/pdf")
    public ResponseEntity<byte[]> verPdf(@PathVariable("personaId") Long personaId,
                                         @PathVariable("libroId") Long libroId) {
        ResponseEntity<byte[]> pdf = personaService.obtenerPdf(personaId, libroId);
        String disposicion = pdf.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders cabecera = new HttpHeaders();
        cabecera.setContentType(MediaType.APPLICATION_PDF);
        cabecera.set(HttpHeaders.CONTENT_DISPOSITION, disposicion != null ? disposicion : "inline");
        cabecera.set("X-Content-Type-Options", "nosniff");
        return new ResponseEntity<>(pdf.getBody(), cabecera, HttpStatus.OK);
    }

    /** Validación de la vista: si se eligió un archivo, debe ser .pdf (el servidor además verifica el contenido). */
    private void validarArchivo(LibroDTO libro, BindingResult result) {
        MultipartFile archivo = libro.getArchivo();
        if (archivo == null || archivo.isEmpty()) {
            return;
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename();
        if (!nombre.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            result.rejectValue("archivo", "archivo.tipo", "El archivo debe ser un PDF (extensión .pdf)");
        }
    }

    private void cargarFormulario(Model model, Long personaId, LibroDTO libro) {
        model.addAttribute("libro", libro);
        model.addAttribute("persona", personaService.obtener(personaId));
        model.addAttribute("autores", autorService.listar());
    }
}

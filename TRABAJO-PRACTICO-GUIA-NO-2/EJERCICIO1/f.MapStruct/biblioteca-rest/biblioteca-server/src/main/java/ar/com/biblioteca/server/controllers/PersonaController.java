package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.entities.Persona;
import ar.com.biblioteca.server.services.PersonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de Personas. Incluye los recursos anidados de sus libros (composición):
 * /api/v1/personas/{personaId}/libros
 */
@RestController
@RequestMapping("/api/v1/personas")
public class PersonaController {

    private final PersonaService service;

    public PersonaController(PersonaService service) {
        this.service = service;
    }

    // ----------------------------------------------------------- Personas

    @GetMapping
    public List<Persona> listar(@RequestParam(name = "filtro", required = false) String filtro) {
        return service.findAll(filtro);
    }

    @GetMapping("/{id}")
    public Persona obtener(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Persona> crear(@Valid @RequestBody Persona persona) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(persona));
    }

    @PutMapping("/{id}")
    public Persona actualizar(@PathVariable("id") Long id, @Valid @RequestBody Persona persona) {
        return service.update(id, persona);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") Long id) {
        service.delete(id);
    }

    // ----------------------------------------------------------- Libros de la persona

    @GetMapping("/{personaId}/libros")
    public List<Libro> listarLibros(@PathVariable("personaId") Long personaId) {
        return service.findLibros(personaId);
    }

    @GetMapping("/{personaId}/libros/{libroId}")
    public Libro obtenerLibro(@PathVariable("personaId") Long personaId,
                              @PathVariable("libroId") Long libroId) {
        return service.findLibro(personaId, libroId);
    }

    @PostMapping("/{personaId}/libros")
    public ResponseEntity<Libro> crearLibro(@PathVariable("personaId") Long personaId,
                                            @Valid @RequestBody Libro libro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLibro(personaId, libro));
    }

    @PutMapping("/{personaId}/libros/{libroId}")
    public Libro actualizarLibro(@PathVariable("personaId") Long personaId,
                                 @PathVariable("libroId") Long libroId,
                                 @Valid @RequestBody Libro libro) {
        return service.updateLibro(personaId, libroId, libro);
    }

    @DeleteMapping("/{personaId}/libros/{libroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarLibro(@PathVariable("personaId") Long personaId,
                              @PathVariable("libroId") Long libroId) {
        service.deleteLibro(personaId, libroId);
    }
}

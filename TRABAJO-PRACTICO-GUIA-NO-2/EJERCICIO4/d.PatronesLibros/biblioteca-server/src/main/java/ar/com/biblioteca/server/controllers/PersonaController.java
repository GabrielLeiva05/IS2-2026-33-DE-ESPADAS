package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.dto.adapter.LibroAdapter;
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
 * <p>
 * REFACTOR (Adapter): los libros entran y salen como LibroDTO. El LibroAdapter, con la Fábrica,
 * convierte el DTO en LibroFisico o LibroDigital según su "tipo".
 * (La persona sigue viajando como entidad: el enunciado pide el Adapter para el DTO ⇄ entidad, y
 * aplicarlo también a Persona/Domicilio no agrega nada al patrón.)
 */
@RestController
@RequestMapping("/api/v1/personas")
public class PersonaController {

    private final PersonaService service;
    private final LibroAdapter libroAdapter;

    public PersonaController(PersonaService service, LibroAdapter libroAdapter) {
        this.service = service;
        this.libroAdapter = libroAdapter;
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
    public List<LibroDTO> listarLibros(@PathVariable("personaId") Long personaId) {
        return libroAdapter.toDTOList(service.findLibros(personaId));
    }

    @GetMapping("/{personaId}/libros/{libroId}")
    public LibroDTO obtenerLibro(@PathVariable("personaId") Long personaId,
                                 @PathVariable("libroId") Long libroId) {
        return libroAdapter.toDTO(service.findLibro(personaId, libroId));
    }

    /** Body: {"tipo":"DIGITAL","titulo":...,"formato":"EPUB","autores":[{"id":1}]} (sin tipo = FISICO) */
    @PostMapping("/{personaId}/libros")
    public ResponseEntity<LibroDTO> crearLibro(@PathVariable("personaId") Long personaId,
                                               @Valid @RequestBody LibroDTO dto) {
        Libro creado = service.createLibro(personaId, libroAdapter.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(libroAdapter.toDTO(creado));
    }

    @PutMapping("/{personaId}/libros/{libroId}")
    public LibroDTO actualizarLibro(@PathVariable("personaId") Long personaId,
                                    @PathVariable("libroId") Long libroId,
                                    @Valid @RequestBody LibroDTO dto) {
        return libroAdapter.toDTO(service.updateLibro(personaId, libroId, libroAdapter.toEntity(dto)));
    }

    @DeleteMapping("/{personaId}/libros/{libroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarLibro(@PathVariable("personaId") Long personaId,
                              @PathVariable("libroId") Long libroId) {
        service.deleteLibro(personaId, libroId);
    }
}

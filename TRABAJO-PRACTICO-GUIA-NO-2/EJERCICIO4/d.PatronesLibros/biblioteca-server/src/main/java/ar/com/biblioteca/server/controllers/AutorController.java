package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.dto.AutorDTO;
import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.dto.adapter.AutorAdapter;
import ar.com.biblioteca.server.dto.adapter.LibroAdapter;
import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.services.AutorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autores")
public class AutorController extends ControladorBase<Autor, AutorDTO, AutorService> {

    private final LibroAdapter libroAdapter;

    public AutorController(AutorService service, AutorAdapter adapter, LibroAdapter libroAdapter) {
        super(service, adapter);
        this.libroAdapter = libroAdapter;
    }

    /** PROTOTYPE: POST /api/v1/autores/{id}/clonar → 201 con el autor nuevo (copia). Sin body. */
    @PostMapping("/{id}/clonar")
    public ResponseEntity<AutorDTO> clonar(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(service.clonar(id)));
    }

    /** ITERATOR: GET /api/v1/autores/{id}/libros → los libros de ese autor. */
    @GetMapping("/{id}/libros")
    public List<LibroDTO> libros(@PathVariable("id") Long id) {
        return libroAdapter.toDTOList(service.librosDelAutor(id));
    }
}

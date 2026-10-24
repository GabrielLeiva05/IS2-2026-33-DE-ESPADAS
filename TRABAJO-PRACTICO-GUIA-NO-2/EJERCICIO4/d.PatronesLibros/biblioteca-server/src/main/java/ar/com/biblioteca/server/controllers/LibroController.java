package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.dto.adapter.LibroAdapter;
import ar.com.biblioteca.server.services.LibroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de libros (solo lectura). El alta/modificación sigue en /personas/{id}/libros.
 * <pre>
 *   GET /api/v1/libros                            → todos
 *   GET /api/v1/libros?criterio=titulo&valor=cien → STRATEGY "titulo"
 *   GET /api/v1/libros?criterio=genero&valor=novela
 *   GET /api/v1/libros?criterio=autor&valor=borges
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final LibroService service;
    private final LibroAdapter adapter;

    public LibroController(LibroService service, LibroAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public List<LibroDTO> buscar(@RequestParam(name = "criterio", required = false) String criterio,
                                 @RequestParam(name = "valor", required = false) String valor) {
        return adapter.toDTOList(service.buscar(criterio, valor));
    }
}

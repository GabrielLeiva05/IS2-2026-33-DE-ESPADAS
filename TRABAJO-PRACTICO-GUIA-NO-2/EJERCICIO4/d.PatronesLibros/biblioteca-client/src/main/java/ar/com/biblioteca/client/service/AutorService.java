package ar.com.biblioteca.client.service;

import ar.com.biblioteca.client.api.ApiClient;
import ar.com.biblioteca.client.dto.AutorDTO;
import ar.com.biblioteca.client.dto.LibroDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consume /api/v1/autores del servidor. */
@Service
public class AutorService {

    private final ApiClient api;

    public AutorService(ApiClient api) {
        this.api = api;
    }

    public List<AutorDTO> listar() {
        return api.getList("/autores", AutorDTO[].class);
    }

    public AutorDTO obtener(Long id) {
        return api.get("/autores/{id}", AutorDTO.class, id);
    }

    public AutorDTO crear(AutorDTO dto) {
        return api.post("/autores", dto, AutorDTO.class);
    }

    public void actualizar(Long id, AutorDTO dto) {
        api.put("/autores/{id}", dto, id);
    }

    public void eliminar(Long id) {
        api.delete("/autores/{id}", id);
    }

    /** Patrón Prototype (en el servidor): crea una copia del autor y devuelve el autor nuevo. */
    public AutorDTO clonar(Long id) {
        return api.post("/autores/{id}/clonar", null, AutorDTO.class, id);
    }

    /** Patrón Iterator (en el servidor): libros escritos por el autor. */
    public List<LibroDTO> librosDelAutor(Long id) {
        return api.getList("/autores/{id}/libros", LibroDTO[].class, id);
    }
}

package ar.com.biblioteca.client.service;

import ar.com.biblioteca.client.api.ApiClient;
import ar.com.biblioteca.client.dto.AutorDTO;
import ar.com.biblioteca.client.dto.LibroConPropietarioDTO;
import ar.com.biblioteca.client.dto.LibroDTO;
import ar.com.biblioteca.client.dto.PersonaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Consume /api/v1/personas (y sus libros anidados) del servidor. */
@Service
public class PersonaService {

    private final ApiClient api;

    public PersonaService(ApiClient api) {
        this.api = api;
    }

    // ------------------------------------------------------------ Personas

    public List<PersonaDTO> listar(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return api.getList("/personas", PersonaDTO[].class);
        }
        return api.getList("/personas?filtro={filtro}", PersonaDTO[].class, filtro.trim());
    }

    public PersonaDTO obtener(Long id) {
        return api.get("/personas/{id}", PersonaDTO.class, id);
    }

    public PersonaDTO crear(PersonaDTO dto) {
        return api.post("/personas", dto, PersonaDTO.class);
    }

    public void actualizar(Long id, PersonaDTO dto) {
        api.put("/personas/{id}", dto, id);
    }

    public void eliminar(Long id) {
        api.delete("/personas/{id}", id);
    }

    // ------------------------------------------------------------ Libros (composición de Persona)

    public LibroDTO obtenerLibro(Long personaId, Long libroId) {
        LibroDTO libro = api.get("/personas/{personaId}/libros/{libroId}", LibroDTO.class, personaId, libroId);
        // Para el formulario: se tildan los autores que ya tiene el libro
        List<Long> ids = new ArrayList<>();
        for (AutorDTO autor : libro.getAutores()) {
            ids.add(autor.getId());
        }
        libro.setAutoresIds(ids);
        return libro;
    }

    public LibroDTO crearLibro(Long personaId, LibroDTO dto) {
        convertirIdsEnAutores(dto);
        return api.post("/personas/{personaId}/libros", dto, LibroDTO.class, personaId);
    }

    public void actualizarLibro(Long personaId, Long libroId, LibroDTO dto) {
        convertirIdsEnAutores(dto);
        api.put("/personas/{personaId}/libros/{libroId}", dto, personaId, libroId);
    }

    public void eliminarLibro(Long personaId, Long libroId) {
        api.delete("/personas/{personaId}/libros/{libroId}", personaId, libroId);
    }

    /** Catálogo completo: todos los libros junto con la persona a la que pertenecen. */
    public List<LibroConPropietarioDTO> listarTodosLosLibros() {
        List<LibroConPropietarioDTO> resultado = new ArrayList<>();
        for (PersonaDTO persona : listar(null)) {
            for (LibroDTO libro : persona.getLibros()) {
                resultado.add(new LibroConPropietarioDTO(libro, persona));
            }
        }
        return resultado;
    }

    /** El formulario trabaja con ids; la API espera la lista de autores (basta con el id de cada uno). */
    private void convertirIdsEnAutores(LibroDTO dto) {
        List<AutorDTO> autores = new ArrayList<>();
        if (dto.getAutoresIds() != null) {
            for (Long id : dto.getAutoresIds()) {
                AutorDTO autor = new AutorDTO();
                autor.setId(id);
                autores.add(autor);
            }
        }
        dto.setAutores(autores);
    }
}

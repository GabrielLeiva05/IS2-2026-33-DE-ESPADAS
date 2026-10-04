package ar.com.biblioteca.client.service;

import ar.com.biblioteca.client.api.ApiClient;
import ar.com.biblioteca.client.dto.AutorDTO;
import ar.com.biblioteca.client.dto.LibroConPropietarioDTO;
import ar.com.biblioteca.client.dto.LibroDTO;
import ar.com.biblioteca.client.dto.PersonaDTO;
import ar.com.biblioteca.client.exception.ApiException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    /**
     * Alta de libro. Si el formulario trae un PDF se envía una petición multipart (partes "libro" + "archivo");
     * si no, el JSON de siempre.
     */
    public LibroDTO crearLibro(Long personaId, LibroDTO dto) {
        convertirIdsEnAutores(dto);
        if (tieneArchivo(dto)) {
            MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
            partes.add("libro", parteJson(dto));
            partes.add("archivo", partePdf(dto.getArchivo()));
            return api.postMultipart("/personas/{personaId}/libros", partes, LibroDTO.class, personaId);
        }
        return api.post("/personas/{personaId}/libros", dto, LibroDTO.class, personaId);
    }

    /**
     * Modificación de libro. Primero se guardan los datos y después, si se eligió un archivo, se reemplaza el PDF
     * (en ese orden para que el nombre del archivo en el servidor use el título ya actualizado).
     */
    public void actualizarLibro(Long personaId, Long libroId, LibroDTO dto) {
        convertirIdsEnAutores(dto);
        api.put("/personas/{personaId}/libros/{libroId}", dto, personaId, libroId);
        if (tieneArchivo(dto)) {
            MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
            partes.add("archivo", partePdf(dto.getArchivo()));
            try {
                api.putMultipart("/personas/{personaId}/libros/{libroId}/pdf", partes, personaId, libroId);
            } catch (ApiException e) {
                throw new ApiException(e.getStatus(),
                        "Los datos del libro se guardaron, pero no se pudo cargar el PDF: " + e.getMessage());
            }
        }
    }

    /** Descarga el PDF del libro desde el servidor (el cliente lo reenvía al navegador). */
    public ResponseEntity<byte[]> obtenerPdf(Long personaId, Long libroId) {
        return api.getBytes("/personas/{personaId}/libros/{libroId}/pdf", personaId, libroId);
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

    private boolean tieneArchivo(LibroDTO dto) {
        return dto.getArchivo() != null && !dto.getArchivo().isEmpty();
    }

    /** Parte "libro" del multipart: los datos del libro como JSON. */
    private HttpEntity<LibroDTO> parteJson(LibroDTO dto) {
        HttpHeaders cabecera = new HttpHeaders();
        cabecera.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(dto, cabecera);
    }

    /**
     * Parte "archivo" del multipart. Se sobrescribe getFilename() porque, sin nombre de archivo, el servidor
     * interpretaría la parte como un campo de texto y no como un archivo.
     */
    private HttpEntity<ByteArrayResource> partePdf(MultipartFile archivo) {
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new ApiException(500, "No se pudo leer el archivo seleccionado");
        }
        ByteArrayResource recurso = new ByteArrayResource(contenido) {
            @Override
            public String getFilename() {
                return "libro.pdf"; // el servidor genera el nombre definitivo (libro_<titulo>_<id>.pdf)
            }
        };
        HttpHeaders cabecera = new HttpHeaders();
        cabecera.setContentType(MediaType.APPLICATION_PDF);
        return new HttpEntity<>(recurso, cabecera);
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

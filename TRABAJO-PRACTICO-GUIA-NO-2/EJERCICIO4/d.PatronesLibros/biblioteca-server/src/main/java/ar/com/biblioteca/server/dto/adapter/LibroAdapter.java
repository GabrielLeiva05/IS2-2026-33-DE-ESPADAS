package ar.com.biblioteca.server.dto.adapter;

import ar.com.biblioteca.server.dto.AutorDTO;
import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.entities.LibroDigital;
import ar.com.biblioteca.server.entities.LibroFisico;
import ar.com.biblioteca.server.exceptions.BadRequestException;
import ar.com.biblioteca.server.factory.LibroCreator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADAPTER de Libro: LibroDTO ⇄ LibroFisico / LibroDigital.
 * <p>
 * toEntity() no hace new de ningún libro: según el "tipo" del DTO elige el LibroCreator que
 * corresponde (Fábrica) y este lo arma con el LibroBuilder (Builder).
 */
@Component
public class LibroAdapter extends BaseAdapter<Libro, LibroDTO> {

    private final AutorAdapter autorAdapter;
    private final Map<String, LibroCreator<?>> creatorsPorTipo = new HashMap<>();

    /** Spring inyecta TODOS los beans LibroCreator (físico y digital) en la lista. */
    public LibroAdapter(List<LibroCreator<?>> creators, AutorAdapter autorAdapter) {
        this.autorAdapter = autorAdapter;
        for (LibroCreator<?> creator : creators) {
            creatorsPorTipo.put(creator.tipo(), creator);
        }
    }

    @Override
    public Libro toEntity(LibroDTO dto) {
        if (dto == null) return null;
        Libro libro = creatorPara(dto.getTipo()).crear(dto, referenciasDeAutores(dto));
        libro.setId(dto.getId());
        return libro;
    }

    @Override
    public LibroDTO toDTO(Libro libro) {
        if (libro == null) return null;
        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setTipo(libro.getTipo());
        dto.setTitulo(libro.getTitulo());
        dto.setFecha(libro.getFecha());
        dto.setGenero(libro.getGenero());
        dto.setPaginas(libro.getPaginas());
        dto.setAutor(libro.getAutor());
        dto.setFechaDevolucion(libro.getFechaDevolucion());
        dto.setAutores(autorAdapter.toDTOList(libro.getAutores()));
        if (libro instanceof LibroFisico fisico) {
            dto.setUbicacion(fisico.getUbicacion());
        } else if (libro instanceof LibroDigital digital) {
            dto.setFormato(digital.getFormato());
        }
        return dto;
    }

    /** Sin tipo → FÍSICO (así siguen funcionando los pedidos de antes del refactor). */
    private LibroCreator<?> creatorPara(String tipo) {
        String clave = (tipo == null || tipo.isBlank()) ? LibroFisico.TIPO : tipo.trim().toUpperCase();
        LibroCreator<?> creator = creatorsPorTipo.get(clave);
        if (creator == null) {
            throw new BadRequestException("Tipo de libro desconocido: " + tipo
                    + ". Valores válidos: " + String.join(", ", creatorsPorTipo.keySet()));
        }
        return creator;
    }

    /** {"autores":[{"id":1}]} → Autores con solo el id (PersonaService los cambia por los reales). */
    private List<Autor> referenciasDeAutores(LibroDTO dto) {
        List<Autor> autores = new ArrayList<>();
        if (dto.getAutores() != null) {
            for (AutorDTO autorDTO : dto.getAutores()) {
                Autor referencia = new Autor();
                referencia.setId(autorDTO == null ? null : autorDTO.getId());
                autores.add(referencia);
            }
        }
        return autores;
    }
}

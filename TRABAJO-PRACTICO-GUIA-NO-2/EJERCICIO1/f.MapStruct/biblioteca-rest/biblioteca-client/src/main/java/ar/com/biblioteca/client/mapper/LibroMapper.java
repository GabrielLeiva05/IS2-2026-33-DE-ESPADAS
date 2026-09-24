package ar.com.biblioteca.client.mapper;

import ar.com.biblioteca.client.dto.AutorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

/** Convierte los autores del formulario a la representación que espera la API y viceversa. */
@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface LibroMapper {

    List<Long> toAutorIds(List<AutorDTO> autores);

    List<AutorDTO> toAutores(List<Long> autoresIds);

    default Long toAutorId(AutorDTO autor) {
        return autor == null ? null : autor.getId();
    }

    default AutorDTO toAutor(Long autorId) {
        if (autorId == null) {
            return null;
        }
        AutorDTO autor = new AutorDTO();
        autor.setId(autorId);
        return autor;
    }
}
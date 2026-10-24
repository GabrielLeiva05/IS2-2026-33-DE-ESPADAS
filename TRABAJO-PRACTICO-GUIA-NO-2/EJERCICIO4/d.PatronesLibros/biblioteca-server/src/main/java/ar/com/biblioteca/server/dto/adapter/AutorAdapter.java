package ar.com.biblioteca.server.dto.adapter;

import ar.com.biblioteca.server.dto.AutorDTO;
import ar.com.biblioteca.server.entities.Autor;
import org.springframework.stereotype.Component;

@Component
public class AutorAdapter extends BaseAdapter<Autor, AutorDTO> {

    @Override
    public AutorDTO toDTO(Autor autor) {
        if (autor == null) return null;
        AutorDTO dto = new AutorDTO();
        dto.setId(autor.getId());
        dto.setNombre(autor.getNombre());
        dto.setApellido(autor.getApellido());
        dto.setBiografia(autor.getBiografia());
        return dto;
    }

    @Override
    public Autor toEntity(AutorDTO dto) {
        if (dto == null) return null;
        Autor autor = new Autor(dto.getNombre(), dto.getApellido(), dto.getBiografia());
        autor.setId(dto.getId());
        return autor;
    }
}

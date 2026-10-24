package ar.com.biblioteca.server.dto.adapter;

import ar.com.biblioteca.server.dto.LocalidadDTO;
import ar.com.biblioteca.server.entities.Localidad;
import org.springframework.stereotype.Component;

@Component
public class LocalidadAdapter extends BaseAdapter<Localidad, LocalidadDTO> {

    @Override
    public LocalidadDTO toDTO(Localidad localidad) {
        if (localidad == null) return null;
        LocalidadDTO dto = new LocalidadDTO();
        dto.setId(localidad.getId());
        dto.setDenominacion(localidad.getDenominacion());
        return dto;
    }

    @Override
    public Localidad toEntity(LocalidadDTO dto) {
        if (dto == null) return null;
        Localidad localidad = new Localidad(dto.getDenominacion());
        localidad.setId(dto.getId());
        return localidad;
    }
}

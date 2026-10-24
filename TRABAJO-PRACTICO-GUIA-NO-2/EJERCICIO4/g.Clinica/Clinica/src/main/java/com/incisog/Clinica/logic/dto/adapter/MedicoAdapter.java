package com.incisog.Clinica.logic.dto.adapter;

import com.incisog.Clinica.logic.dto.MedicoDTO;
import com.incisog.Clinica.logic.factory.MedicoCreator;
import com.incisog.Clinica.models.Medico;
import org.springframework.stereotype.Component;

@Component
public class MedicoAdapter extends BaseAdapter<Medico, MedicoDTO> {

    private final MedicoCreator creator;

    public MedicoAdapter(MedicoCreator creator) {
        this.creator = creator;
    }

    /** La entidad la FABRICA el creator (Factory Method); el adapter solo traduce el DTO. */
    @Override
    public Medico toEntity(MedicoDTO dto) {
        if (dto == null) return null;
        Medico medico = creator.crear(dto.getNombre(), dto.getApellido(), dto.getDocumento());
        medico.setId(dto.getId());
        return medico;
    }

    @Override
    public MedicoDTO toDTO(Medico medico) {
        if (medico == null) return null;
        MedicoDTO dto = new MedicoDTO();
        dto.setId(medico.getId());
        dto.setNombre(medico.getNombre());
        dto.setApellido(medico.getApellido());
        dto.setDocumento(medico.getDocumento());
        return dto;
    }
}

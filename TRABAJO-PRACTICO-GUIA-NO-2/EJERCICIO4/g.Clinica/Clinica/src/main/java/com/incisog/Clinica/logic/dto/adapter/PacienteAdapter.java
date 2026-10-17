package com.incisog.Clinica.logic.dto.adapter;

import com.incisog.Clinica.logic.dto.PacienteDTO;
import com.incisog.Clinica.logic.factory.PacienteCreator;
import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Component;

@Component
public class PacienteAdapter extends BaseAdapter<Paciente, PacienteDTO> {

    private final PacienteCreator creator;

    public PacienteAdapter(PacienteCreator creator) {
        this.creator = creator;
    }

    /** La entidad la FABRICA el creator (Factory Method); el adapter solo traduce el DTO. */
    @Override
    public Paciente toEntity(PacienteDTO dto) {
        if (dto == null) return null;
        Paciente paciente = creator.crear(dto.getNombre(), dto.getApellido(), dto.getDocumento());
        paciente.setId(dto.getId());
        return paciente;
    }

    @Override
    public PacienteDTO toDTO(Paciente paciente) {
        if (paciente == null) return null;
        PacienteDTO pacienteDTO = new PacienteDTO();
        pacienteDTO.setId(paciente.getId());
        pacienteDTO.setNombre(paciente.getNombre());
        pacienteDTO.setApellido(paciente.getApellido());
        pacienteDTO.setDocumento(paciente.getDocumento());
        pacienteDTO.setTieneFoto(paciente.getFoto() != null);
        return pacienteDTO;
    }
}

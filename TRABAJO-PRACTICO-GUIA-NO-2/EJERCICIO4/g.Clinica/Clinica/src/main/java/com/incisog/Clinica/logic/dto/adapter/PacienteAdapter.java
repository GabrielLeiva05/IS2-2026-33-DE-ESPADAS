package com.incisog.Clinica.logic.dto.adapter;

import com.incisog.Clinica.logic.dto.PacienteDTO;
import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PacienteAdapter extends BaseAdapter<Paciente, PacienteDTO> {

    @Override
    public Paciente toEntity(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setId(dto.getId());
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setDocumento(dto.getDocumento());
        return paciente;
    }

    @Override
    public PacienteDTO toDTO(Paciente paciente){
        PacienteDTO pacienteDTO = new PacienteDTO();
        pacienteDTO.setId(paciente.getId());
        pacienteDTO.setNombre(paciente.getNombre());
        pacienteDTO.setApellido(paciente.getApellido());
        pacienteDTO.setDocumento(paciente.getDocumento());

        pacienteDTO.setTieneFoto(paciente.getFoto() != null);
        return pacienteDTO;
    }

}

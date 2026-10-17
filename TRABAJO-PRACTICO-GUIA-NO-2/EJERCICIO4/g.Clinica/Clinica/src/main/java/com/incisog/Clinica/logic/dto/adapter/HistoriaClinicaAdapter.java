package com.incisog.Clinica.logic.dto.adapter;

import com.incisog.Clinica.logic.dto.HistoriaClinicaDTO;
import com.incisog.Clinica.models.HistoriaClinica;
import com.incisog.Clinica.models.Paciente;
import com.incisog.Clinica.models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class HistoriaClinicaAdapter extends BaseAdapter<HistoriaClinica, HistoriaClinicaDTO> {

    @Override
    public HistoriaClinicaDTO toDTO(HistoriaClinica historia) {
        if (historia == null) return null;
        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setId(historia.getId());
        dto.setPacienteId(historia.getPaciente() != null ? historia.getPaciente().getId() : null);
        dto.setUsuarioId(historia.getUsuario() != null ? historia.getUsuario().getId() : null);
        return dto;
    }

    /**
     * Arma una historia con un Paciente y un Usuario que solo tienen el id.
     * Es la misma forma que antes armaba Jackson con {"paciente":{"id":...}}, así que
     * validar() y antesDeCrear() del service (que buscan las entidades reales) no cambian.
     */
    @Override
    public HistoriaClinica toEntity(HistoriaClinicaDTO dto) {
        if (dto == null) return null;
        HistoriaClinica historia = new HistoriaClinica();
        historia.setId(dto.getId());
        if (dto.getPacienteId() != null) {
            Paciente paciente = new Paciente();
            paciente.setId(dto.getPacienteId());
            historia.setPaciente(paciente);
        }
        if (dto.getUsuarioId() != null) {
            Usuario usuario = new Usuario();
            usuario.setId(dto.getUsuarioId());
            historia.setUsuario(usuario);
        }
        return historia;
    }
}

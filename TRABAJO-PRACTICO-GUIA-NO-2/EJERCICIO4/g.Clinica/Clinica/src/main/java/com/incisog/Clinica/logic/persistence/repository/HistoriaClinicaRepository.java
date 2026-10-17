package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.HistoriaClinica;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends BaseRepositoryTemplate<HistoriaClinica> {

    // "PacienteId" navega la relación: historia.paciente.id
    boolean existsByPacienteIdAndEliminadoFalse(String pacienteId);

    Optional<HistoriaClinica> findByPacienteIdAndEliminadoFalse(String pacienteId);
}

package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica,String> {
    List<HistoriaClinica> findByEliminadoFalse();

    // "PacienteId" navega la relación: historia.paciente.id
    boolean existsByPacienteIdAndEliminadoFalse(String pacienteId);

    Optional<HistoriaClinica> findByPacienteIdAndEliminadoFalse(String pacienteId);
}

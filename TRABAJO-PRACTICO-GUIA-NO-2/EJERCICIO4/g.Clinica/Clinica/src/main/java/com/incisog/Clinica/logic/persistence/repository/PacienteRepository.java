package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, String> {

    List<Paciente> findByEliminadoFalse();

    boolean existsByDocumentoAndEliminadoFalse(String documento);

}

package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.Paciente;

/**
 * findByEliminadoFalse(), findById(), save()... se heredan de BaseRepositoryTemplate / JpaRepository.
 * Acá solo van las consultas propias de Paciente.
 */
public interface PacienteRepository extends BaseRepositoryTemplate<Paciente> {

    boolean existsByDocumentoAndEliminadoFalse(String documento);
}

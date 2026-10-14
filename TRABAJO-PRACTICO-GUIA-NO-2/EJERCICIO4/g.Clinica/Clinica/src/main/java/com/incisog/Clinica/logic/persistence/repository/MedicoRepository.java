package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {

    public List<Medico> findByEliminadoFalse();
    public boolean existsByDocumentoAndEliminadoFalse(String documento);
}

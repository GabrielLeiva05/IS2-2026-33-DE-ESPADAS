package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.DetalleHistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleHistoriaClinicaRepository extends JpaRepository<DetalleHistoriaClinica, String> {
}

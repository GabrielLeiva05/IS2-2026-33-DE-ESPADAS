package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioContactoTelefonico extends JpaRepository<ContactoTelefonico, String> {
    List<ContactoTelefonico> findByEliminadoFalse();
    List<ContactoTelefonico> findByPersonaId(String personaId);
}

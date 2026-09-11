package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.ContactoCorreoElectronico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioContactoCorreoElectronico extends JpaRepository<ContactoCorreoElectronico, String> {
    List<ContactoCorreoElectronico> findByEliminadoFalse();
    List<ContactoCorreoElectronico> findByPersonaId(String personaId);
}

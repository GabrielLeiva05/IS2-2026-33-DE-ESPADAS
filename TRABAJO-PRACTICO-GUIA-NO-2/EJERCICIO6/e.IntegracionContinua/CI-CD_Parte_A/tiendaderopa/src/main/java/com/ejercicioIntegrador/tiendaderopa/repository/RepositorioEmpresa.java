package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioEmpresa extends JpaRepository<Empresa, String> {
    List<Empresa> findByEliminadoFalse();
    Optional<Empresa> findByRazonSocialIgnoreCase(String razonSocial);
    boolean existsByCuit(String cuit);
}
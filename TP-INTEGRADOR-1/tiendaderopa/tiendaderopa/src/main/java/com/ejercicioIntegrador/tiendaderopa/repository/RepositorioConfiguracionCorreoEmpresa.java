package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.ConfiguracionCorreoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioConfiguracionCorreoEmpresa extends JpaRepository<ConfiguracionCorreoEmpresa, String> {
    List<ConfiguracionCorreoEmpresa> findByEliminadoFalse();
    List<ConfiguracionCorreoEmpresa> findByEmpresaId(String empresaId);
}
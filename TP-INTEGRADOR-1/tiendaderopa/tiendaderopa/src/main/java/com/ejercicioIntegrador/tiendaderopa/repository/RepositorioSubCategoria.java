package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioSubCategoria extends JpaRepository<SubCategoria, Long> {
}

package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioSubCategoria extends JpaRepository<SubCategoria, String> {
	Optional<SubCategoria> findByCategoria_IdAndNombre(String categoriaId, String nombre);
}

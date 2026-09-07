package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioCategoria extends JpaRepository<Categoria, Long> {
}
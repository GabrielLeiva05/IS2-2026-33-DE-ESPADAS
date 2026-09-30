package com.ejercicioIntegrador.tiendaderopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ejercicioIntegrador.tiendaderopa.model.Nacionalidad;
import java.util.List;
import java.util.Optional;

@Repository
public interface NacionalidadRepository extends JpaRepository<Nacionalidad, String> {
    Optional<Nacionalidad> findByNombreIgnoreCase(String nombre);
    List<Nacionalidad> findByEliminadoFalse();
}
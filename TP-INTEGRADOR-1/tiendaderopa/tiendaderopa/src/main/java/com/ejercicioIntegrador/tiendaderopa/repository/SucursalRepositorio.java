package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepositorio extends JpaRepository<Sucursal, String> {
    Optional<Sucursal> findFirstByActivaTrueAndPrincipalTrueOrderByNombreAsc();

    List<Sucursal> findByActivaTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** ARCHIVO NUEVO. Solo ServicioProveedor lo inyecta (regla de capas). */
@Repository
public interface RepositorioProveedor extends JpaRepository<Proveedor, String> {
    List<Proveedor> findByEliminadoFalse();
    Proveedor findByRazonSocial(String razonSocial);
}

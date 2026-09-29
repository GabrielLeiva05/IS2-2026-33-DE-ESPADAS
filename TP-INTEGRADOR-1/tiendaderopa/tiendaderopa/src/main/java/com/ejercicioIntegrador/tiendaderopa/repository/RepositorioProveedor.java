package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioProveedor extends JpaRepository<Proveedor, String> {
    List<Proveedor> findByEliminadoFalse();
}

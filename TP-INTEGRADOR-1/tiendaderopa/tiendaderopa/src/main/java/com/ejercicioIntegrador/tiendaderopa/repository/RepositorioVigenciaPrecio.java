package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioVigenciaPrecio extends JpaRepository<VigenciaPrecio, String> {

    List<VigenciaPrecio> findByEliminadoFalse();

    // Navega la relación producto.id automáticamente (property path)
    Optional<VigenciaPrecio> findByProducto_IdAndFechaHastaIsNull(String idProducto);
}
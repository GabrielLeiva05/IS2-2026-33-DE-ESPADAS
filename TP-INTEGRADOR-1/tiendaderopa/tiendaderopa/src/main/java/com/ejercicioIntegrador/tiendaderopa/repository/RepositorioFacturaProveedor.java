package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioFacturaProveedor extends JpaRepository<FacturaProveedor, String> {
    List<FacturaProveedor> findByEliminadoFalse();
    List<FacturaProveedor> findByEstadoFactura(EstadoFactura estado);
}

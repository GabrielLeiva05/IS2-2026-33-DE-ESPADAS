package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RepositorioFacturaCliente extends JpaRepository<FacturaCliente, String> {
    List<FacturaCliente> findByEliminadoFalse();
    List<FacturaCliente> findByEstadoFactura(EstadoFactura estado);
}

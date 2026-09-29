package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioFactura extends JpaRepository<Factura, String> {
    List<Factura> findByEliminadoFalse();

    List<Factura> findByEstadoFactura(EstadoFactura estado);

    Optional<Factura> findByNumeroFactura(Long numeroFactura);
}

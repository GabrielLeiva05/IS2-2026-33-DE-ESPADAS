package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDetalleFactura extends JpaRepository<DetalleFactura, String> {

    // Obtener solo los detalles activos
    List<DetalleFactura> findByEliminadoFalse();

    // Obtener los detalles pertenecientes a una factura específica
    List<DetalleFactura> findByFacturaIdAndEliminadoFalse(String facturaId);
}
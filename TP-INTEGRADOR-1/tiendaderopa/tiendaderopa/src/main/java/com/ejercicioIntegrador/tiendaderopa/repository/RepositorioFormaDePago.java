package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.model.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioFormaDePago extends JpaRepository<FormaDePago, String> {
    Optional<FormaDePago> findByTipoPagoAndEliminadoFalse(TipoPago tipoPago); // NUEVO
}

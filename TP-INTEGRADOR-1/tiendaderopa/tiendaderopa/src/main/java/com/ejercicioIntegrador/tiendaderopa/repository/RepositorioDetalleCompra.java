package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDetalleCompra extends JpaRepository<DetalleCompra, String> {

    List<DetalleCompra> findByEliminadoFalse();

    List<DetalleCompra> findByOrdenCompraIdAndEliminadoFalse(String ordenCompraId);
}
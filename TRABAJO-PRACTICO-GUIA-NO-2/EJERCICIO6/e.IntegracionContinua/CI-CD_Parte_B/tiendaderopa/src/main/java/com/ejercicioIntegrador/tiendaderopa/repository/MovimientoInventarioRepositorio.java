package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.MovimientoInventario;
import com.ejercicioIntegrador.tiendaderopa.model.TipoMovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, String> {
    boolean existsByTipoAndReferencia(TipoMovimientoInventario tipo, String referencia);

    List<MovimientoInventario> findByProducto_Id(String productoId);
}
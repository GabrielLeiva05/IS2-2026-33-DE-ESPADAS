package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioStock extends JpaRepository<Stock, String> {
    Optional<Stock> findTopByDetalleFactura_Producto_IdAndEliminadoFalseOrderByFechaMovimientoDesc(String idProducto);

    boolean existsByDetalleFactura_IdAndEliminadoFalse(String idDetalleFactura);

    List<Stock> findByEliminadoFalse();

    List<Stock> findByDetalleFactura_Producto_IdAndEliminadoFalseAndFechaMovimientoAfterOrderByFechaMovimientoAsc(
            String idProducto,
            LocalDateTime fechaMovimiento
    );
}
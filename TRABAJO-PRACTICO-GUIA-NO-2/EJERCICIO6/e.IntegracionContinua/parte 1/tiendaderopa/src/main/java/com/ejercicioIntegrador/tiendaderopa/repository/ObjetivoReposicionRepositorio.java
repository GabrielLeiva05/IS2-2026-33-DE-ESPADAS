package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.ObjetivoReposicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ObjetivoReposicionRepositorio extends JpaRepository<ObjetivoReposicion, String> {
    Optional<ObjetivoReposicion> findBySucursal_IdAndProducto_Id(String sucursalId, String productoId);

    List<ObjetivoReposicion> findByProducto_Id(String productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ObjetivoReposicion o where o.sucursal.id = :sucursalId and o.producto.id = :productoId")
    Optional<ObjetivoReposicion> bloquearPorSucursalYProducto(
            @Param("sucursalId") String sucursalId, @Param("productoId") String productoId);
}
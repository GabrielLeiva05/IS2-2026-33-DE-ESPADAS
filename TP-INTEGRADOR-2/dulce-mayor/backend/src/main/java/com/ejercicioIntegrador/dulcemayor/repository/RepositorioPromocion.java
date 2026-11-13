package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.CodigoDescuento;
import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPromocion extends BaseRepository<Promocion> {

    List<Promocion> findByEliminadoFalseOrderByFechaInicioDesc();

    /** Promociones automáticas (porcentuales y por volumen) vigentes en la fecha. */
    @Query("SELECT p FROM Promocion p WHERE p.eliminado = false AND p.activa = true AND p.fechaInicio <= :fecha "
            + "AND p.fechaFin >= :fecha AND TYPE(p) IN (PromocionPorcentual, PromocionVolumen)")
    List<Promocion> automaticasVigentes(@Param("fecha") LocalDate fecha);

    @Query("SELECT c FROM CodigoDescuento c WHERE UPPER(c.codigo) = UPPER(:codigo) AND c.eliminado = false")
    Optional<CodigoDescuento> buscarCodigo(@Param("codigo") String codigo);

    /** Bloquea el código para contar un uso sin superar el máximo cuando se cargan pedidos a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CodigoDescuento c WHERE c.id = :id")
    Optional<CodigoDescuento> bloquearCodigo(@Param("id") String id);
}

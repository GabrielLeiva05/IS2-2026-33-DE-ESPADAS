package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioPago extends BaseRepository<Pago> {

    List<Pago> findByPedido_IdAndEliminadoFalseOrderByFechaCreacionDesc(String idPedido);

    Optional<Pago> findFirstByPedido_IdAndEstadoAndEliminadoFalse(String idPedido, EstadoPago estado);

    Optional<Pago> findFirstByReferenciaExternaAndEliminadoFalse(String referenciaExterna);

    Optional<Pago> findFirstByReferenciaOperacionAndEliminadoFalse(String referenciaOperacion);

    List<Pago> findByEstadoAndFechaVencimientoBeforeAndEliminadoFalse(EstadoPago estado, LocalDateTime momento);

    /** Serializa la acreditaciÃ³n y el vencimiento del mismo pago (race de webhook contra expiraciÃ³n). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pago p WHERE p.id = :id")
    Optional<Pago> buscarParaActualizar(@Param("id") String id);
}

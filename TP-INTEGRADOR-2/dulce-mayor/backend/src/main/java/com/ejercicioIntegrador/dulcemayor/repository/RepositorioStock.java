package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Stock;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioStock extends BaseRepository<Stock> {

    Optional<Stock> findByProducto_IdAndEliminadoFalse(String idProducto);

    List<Stock> findByEliminadoFalseAndCantidadActualLessThanEqual(int cantidad);

    /** Bloquea la fila para que dos descuentos simultaneos no dejen el stock negativo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.producto.id = :idProducto AND s.eliminado = false")
    Optional<Stock> buscarParaActualizarPorProducto(@Param("idProducto") String idProducto);
}

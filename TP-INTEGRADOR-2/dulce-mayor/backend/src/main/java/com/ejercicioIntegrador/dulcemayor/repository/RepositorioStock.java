package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Stock;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioStock extends BaseRepository<Stock> {

    Optional<Stock> findByProducto_IdAndEliminadoFalse(String idProducto);

    List<Stock> findByEliminadoFalseAndCantidadActualLessThanEqual(int cantidad);

    @Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM ReservaStock r WHERE r.producto.id = :idProducto AND r.eliminado = false AND r.consumida = false AND r.fechaLiberacion IS NULL")
    Long sumarReservasActivas(@Param("idProducto") String idProducto);

    @Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM ReservaStock r WHERE r.producto.id = :idProducto AND r.eliminado = false AND r.consumida = false AND r.fechaLiberacion IS NULL AND r.pedido.estado = com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido.CONFIRMADO")
    Long sumarReservasConfirmadas(@Param("idProducto") String idProducto);

    @Query("SELECT COALESCE(SUM(r.cantidad), 0) FROM ReservaStock r WHERE r.producto.id = :idProducto AND r.pedido.id <> :idPedido AND r.eliminado = false AND r.consumida = false AND r.fechaLiberacion IS NULL")
    Long sumarReservasActivasExceptoPedido(@Param("idProducto") String idProducto, @Param("idPedido") String idPedido);

    /** Bloquea la fila para que dos descuentos simultaneos no dejen el stock negativo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.producto.id = :idProducto AND s.eliminado = false")
    Optional<Stock> buscarParaActualizarPorProducto(@Param("idProducto") String idProducto);
}

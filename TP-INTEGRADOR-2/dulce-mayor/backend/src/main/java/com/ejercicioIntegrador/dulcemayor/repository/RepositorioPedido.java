package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPedido extends BaseRepository<Pedido> {

    Optional<Pedido> findTopByOrderByNumeroPedidoDesc();

    /**
     * Bloquea el pedido mientras cambia de estado: dos pedidos de confirmación/anulación simultáneos
     * se ejecutan uno detrás del otro, así el stock nunca se devuelve dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id AND p.eliminado = false")
    Optional<Pedido> buscarParaActualizar(@Param("id") String id);

    List<Pedido> findByPuntoVenta_Cliente_IdAndEliminadoFalseOrderByFechaHoraCargaDesc(String idCliente);

    /** ¿Hay pedidos sin terminar (cargados o pendientes de entrega) con ese producto? Protege la baja del producto. */
    @Query("SELECT COUNT(d) > 0 FROM DetallePedido d WHERE d.producto.id = :idProducto AND d.pedido.eliminado = false "
            + "AND d.pedido.estado IN :estados")
    boolean existenPedidosConProducto(@Param("idProducto") String idProducto,
                                      @Param("estados") java.util.Collection<EstadoPedido> estados);

    /**
     * Pedidos cargados en un período (gráfico de barras HU-43): preventista y fecha de carga, sin los anulados.
     * Devuelve filas [idPreventista, nombre, apellido, fechaHoraCarga]; la agrupación por día la hace el servicio.
     */
    @Query("SELECT p.preventista.id, p.preventista.nombre, p.preventista.apellido, p.fechaHoraCarga FROM Pedido p "
            + "WHERE p.eliminado = false AND p.estado <> :excluido AND p.fechaHoraCarga >= :desde AND p.fechaHoraCarga < :hasta")
    List<Object[]> pedidosCargadosEntre(@Param("desde") java.time.LocalDateTime desde,
                                        @Param("hasta") java.time.LocalDateTime hasta,
                                        @Param("excluido") EstadoPedido excluido);

    List<Pedido> findByPreventista_IdAndEliminadoFalseOrderByFechaHoraCargaDesc(String idPreventista);

    List<Pedido> findByEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(EstadoPedido estado);

    List<Pedido> findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(String idDistribuidor,
                                                                                         EstadoPedido estado);

    /** Pedidos cargados en el intervalo [desde, hasta) para los reportes administrativos. */
    List<Pedido> findByEliminadoFalseAndFechaHoraCargaGreaterThanEqualAndFechaHoraCargaLessThanOrderByFechaHoraCargaAsc(
            LocalDateTime desde, LocalDateTime hastaExclusivo);

    /** Pedidos confirmados (facturados) en el intervalo [desde, hasta) para el histórico de ventas. */
    List<Pedido> findByEliminadoFalseAndFechaFacturacionGreaterThanEqualAndFechaFacturacionLessThanOrderByFechaFacturacionAsc(
            LocalDateTime desde, LocalDateTime hastaExclusivo);
}

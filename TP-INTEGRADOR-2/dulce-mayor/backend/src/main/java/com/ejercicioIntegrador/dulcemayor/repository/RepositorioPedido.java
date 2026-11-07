package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositorioPedido extends BaseRepository<Pedido> {

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    List<Pedido> findByPreventista_IdAndEliminadoFalseOrderByIdAsc(String idPreventista);

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    Optional<Pedido> findByIdAndPreventista_IdAndEliminadoFalse(String idPedido, String idPreventista);

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    Optional<Pedido> findByIdAndEliminadoFalse(String idPedido);

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    List<Pedido> findByEliminadoFalseOrderByFechaCreacionDesc();

    long countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalse(String codigo,
                                                                      Collection<EstadoPedido> estados);

    long countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalseAndIdNot(String codigo,
                                                                              Collection<EstadoPedido> estados,
                                                                              String idPedido);

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    Optional<Pedido> findByIdAndPuntoVenta_EmailContactoIgnoreCaseAndEliminadoFalse(String idPedido, String email);

    @EntityGraph(attributePaths = {"puntoVenta", "preventista", "lineas"})
    List<Pedido> findByPuntoVenta_EmailContactoIgnoreCaseAndEliminadoFalseOrderByFechaCreacionDesc(String email);

    List<Pedido> findByEstadoAndEliminadoFalse(EstadoPedido estado);
}

package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoParada;
import com.ejercicioIntegrador.dulcemayor.model.Parada;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface RepositorioParada extends BaseRepository<Parada> {

    @EntityGraph(attributePaths = {"ruta", "ruta.distribuidor", "pedido", "pedido.puntoVenta",
            "pedido.puntoVenta.cliente", "pedido.puntoVenta.direccion",
            "pedido.puntoVenta.direccion.localidad"})
    Optional<Parada> findByIdAndEliminadoFalse(String id);

    List<Parada> findByRuta_IdAndEliminadoFalseOrderByOrdenAsc(String idRuta);

    boolean existsByPedido_IdAndEstadoAndEliminadoFalse(String idPedido, EstadoParada estado);

    boolean existsByPedido_IdAndEstadoNotAndEliminadoFalse(String idPedido, EstadoParada estado);
}

package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoRuta;
import com.ejercicioIntegrador.dulcemayor.model.Ruta;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositorioRuta extends BaseRepository<Ruta> {

    @EntityGraph(attributePaths = {"distribuidor"})
    List<Ruta> findByFechaAndEliminadoFalseOrderByFechaAsc(LocalDate fecha);

    @EntityGraph(attributePaths = {"distribuidor"})
    List<Ruta> findByEliminadoFalse();

    Optional<Ruta> findFirstByFechaAndDistribuidor_IdAndEliminadoFalse(LocalDate fecha, String idDistribuidor);

    Optional<Ruta> findFirstByDistribuidor_IdAndEstadoNotAndEliminadoFalseOrderByFechaDesc(String idDistribuidor,
                                                                                          EstadoRuta estado);

    @EntityGraph(attributePaths = {"distribuidor", "paradas", "paradas.pedido",
            "paradas.pedido.puntoVenta", "paradas.pedido.puntoVenta.cliente",
            "paradas.pedido.puntoVenta.direccion", "paradas.pedido.puntoVenta.direccion.localidad"})
    Optional<Ruta> findByIdAndEliminadoFalse(String id);
}

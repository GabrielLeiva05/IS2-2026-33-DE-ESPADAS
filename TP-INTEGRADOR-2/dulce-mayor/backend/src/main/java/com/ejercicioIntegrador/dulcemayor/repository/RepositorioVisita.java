package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Visita;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioVisita extends BaseRepository<Visita> {

    @EntityGraph(attributePaths = {"puntoVenta", "puntoVenta.cliente", "puntoVenta.direccion",
            "puntoVenta.direccion.localidad"})
    List<Visita> findByPreventista_IdAndFechaAndEliminadoFalseOrderByOrdenAsc(String idPreventista, LocalDate fecha);

    List<Visita> findByPreventista_IdAndFechaAndEliminadoFalse(String idPreventista, LocalDate fecha);
}

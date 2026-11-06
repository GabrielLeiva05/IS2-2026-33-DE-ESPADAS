package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPuntoVenta extends BaseRepository<PuntoVenta> {

    Optional<PuntoVenta> findFirstByCuitAndEliminadoFalse(String cuit);

    List<PuntoVenta> findByCliente_IdAndEliminadoFalseOrderByNombreFantasiaAsc(String idCliente);
}

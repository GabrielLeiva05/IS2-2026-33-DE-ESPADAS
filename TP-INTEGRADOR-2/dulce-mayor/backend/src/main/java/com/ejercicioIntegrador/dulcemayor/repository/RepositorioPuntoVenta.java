package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioPuntoVenta extends BaseRepository<PuntoVenta> {

    List<PuntoVenta> findByPreventista_IdAndEliminadoFalseOrderByNombreComercialAsc(String idPreventista);

    List<PuntoVenta> findByCliente_IdAndEliminadoFalseOrderByNombreComercialAsc(String idCliente);

    java.util.Optional<PuntoVenta> findByIdAndEliminadoFalse(String id);
}

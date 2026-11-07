package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;

public interface RepositorioPuntoVenta extends BaseRepository<PuntoVenta> {

    List<PuntoVenta> findByPreventista_IdAndEliminadoFalseOrderByNombreComercialAsc(String idPreventista);

    List<PuntoVenta> findByCliente_IdAndEliminadoFalseOrderByNombreComercialAsc(String idCliente);

    java.util.Optional<PuntoVenta> findByIdAndEliminadoFalse(String id);
}

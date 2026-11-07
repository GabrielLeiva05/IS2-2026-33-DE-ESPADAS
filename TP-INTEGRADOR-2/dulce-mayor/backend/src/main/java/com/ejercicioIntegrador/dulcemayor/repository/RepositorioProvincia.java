package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Provincia;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioProvincia extends BaseRepository<Provincia> {

    Optional<Provincia> findFirstByPais_IdAndNombreIgnoreCaseAndEliminadoFalse(String idPais, String nombre);

    Optional<Provincia> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Provincia> findByPais_IdOrderByNombreAsc(String idPais);

    List<Provincia> findByPais_IdAndEliminadoFalseOrderByNombreAsc(String idPais);
}

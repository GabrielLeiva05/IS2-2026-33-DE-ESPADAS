package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Localidad;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioLocalidad extends BaseRepository<Localidad> {

    Optional<Localidad> findFirstByDepartamento_IdAndNombreIgnoreCaseAndEliminadoFalse(String idDepartamento, String nombre);

    Optional<Localidad> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    Optional<Localidad> findFirstByCodigoPostalIgnoreCaseAndEliminadoFalse(String codigoPostal);

    List<Localidad> findByDepartamento_IdOrderByNombreAsc(String idDepartamento);

    List<Localidad> findByDepartamento_IdAndEliminadoFalseOrderByNombreAsc(String idDepartamento);
}

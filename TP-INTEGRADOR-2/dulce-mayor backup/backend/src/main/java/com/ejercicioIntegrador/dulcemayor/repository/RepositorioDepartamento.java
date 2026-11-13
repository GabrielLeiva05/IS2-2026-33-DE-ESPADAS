package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Departamento;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioDepartamento extends BaseRepository<Departamento> {

    Optional<Departamento> findFirstByProvincia_IdAndNombreIgnoreCaseAndEliminadoFalse(String idProvincia, String nombre);

    Optional<Departamento> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Departamento> findByProvincia_IdOrderByNombreAsc(String idProvincia);

    List<Departamento> findByProvincia_IdAndEliminadoFalseOrderByNombreAsc(String idProvincia);
}

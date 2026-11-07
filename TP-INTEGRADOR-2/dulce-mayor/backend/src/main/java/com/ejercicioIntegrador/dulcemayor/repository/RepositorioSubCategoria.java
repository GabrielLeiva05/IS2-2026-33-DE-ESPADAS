package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioSubCategoria extends BaseRepository<SubCategoria> {

    Optional<SubCategoria> findFirstByCategoria_IdAndNombreIgnoreCaseAndEliminadoFalse(String idCategoria, String nombre);

    Optional<SubCategoria> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<SubCategoria> findByCategoria_IdOrderByNombreAsc(String idCategoria);

    List<SubCategoria> findByCategoria_IdAndEliminadoFalseOrderByNombreAsc(String idCategoria);
}

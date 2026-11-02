package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioSubCategoria extends BaseRepository<SubCategoria> {

    Optional<SubCategoria> findFirstByCategoria_IdAndNombreIgnoreCaseAndEliminadoFalse(String idCategoria, String nombre);

    Optional<SubCategoria> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<SubCategoria> findByCategoria_IdOrderByNombreAsc(String idCategoria);

    List<SubCategoria> findByCategoria_IdAndEliminadoFalseOrderByNombreAsc(String idCategoria);
}

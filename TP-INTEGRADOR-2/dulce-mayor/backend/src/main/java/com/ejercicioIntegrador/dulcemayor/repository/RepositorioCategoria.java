package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioCategoria extends BaseRepository<Categoria> {

    Optional<Categoria> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);
}

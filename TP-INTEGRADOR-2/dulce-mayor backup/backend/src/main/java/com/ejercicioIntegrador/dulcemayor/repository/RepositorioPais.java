package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Pais;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioPais extends BaseRepository<Pais> {

    Optional<Pais> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);
}

package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioDireccion extends BaseRepository<Direccion> {

    Optional<Direccion> findFirstByCalleIgnoreCaseAndNumeracionIgnoreCaseAndEliminadoFalse(String calle, String numeracion);
}

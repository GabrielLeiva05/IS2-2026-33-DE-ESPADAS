package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.Optional;

public interface RepositorioDireccion extends BaseRepository<Direccion> {

    Optional<Direccion> findFirstByCalleIgnoreCaseAndNumeracionIgnoreCaseAndEliminadoFalse(String calle, String numeracion);
}

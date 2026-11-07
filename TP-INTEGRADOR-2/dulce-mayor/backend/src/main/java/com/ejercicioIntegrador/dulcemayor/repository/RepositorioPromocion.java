package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.Optional;

public interface RepositorioPromocion extends BaseRepository<Promocion> {

    Optional<Promocion> findFirstByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    boolean existsByCodigoIgnoreCaseAndEliminadoFalse(String codigo);
}

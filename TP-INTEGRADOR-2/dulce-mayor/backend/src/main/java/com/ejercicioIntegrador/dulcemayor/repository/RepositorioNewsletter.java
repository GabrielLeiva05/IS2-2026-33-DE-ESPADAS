package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.SuscripcionNewsletter;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioNewsletter extends BaseRepository<SuscripcionNewsletter> {

    Optional<SuscripcionNewsletter> findByEmailIgnoreCaseAndEliminadoFalse(String email);

    Optional<SuscripcionNewsletter> findByTokenBajaAndEliminadoFalse(String tokenBaja);

    List<SuscripcionNewsletter> findByActivaTrueAndEliminadoFalse();
}

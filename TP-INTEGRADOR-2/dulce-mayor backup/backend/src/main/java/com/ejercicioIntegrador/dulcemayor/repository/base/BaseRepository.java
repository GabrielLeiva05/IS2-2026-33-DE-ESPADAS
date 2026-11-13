package com.ejercicioIntegrador.dulcemayor.repository.base;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio generico: agrega a JpaRepository las consultas que necesita BaseService
 * para trabajar con la baja logica de cualquier entidad.
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends JpaRepository<E, String> {

    List<E> findByEliminadoFalse();

    Optional<E> findByIdAndEliminadoFalse(String id);
}

package com.uncuyo.tp1_ej4.repositories;

import com.uncuyo.tp1_ej4.entities.EntidadBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Template Method de acceso común: fija cómo buscar un registro activo y usa
 * findById, provisto por Spring Data, como paso variable de persistencia.
 */
@NoRepositoryBean
public interface RepositorioBase<E extends EntidadBase> extends JpaRepository<E, Long> {

    default Optional<E> findByIdAndActivo(long id) {
        return findById(id).filter(EntidadBase::isActivo);
    }
}

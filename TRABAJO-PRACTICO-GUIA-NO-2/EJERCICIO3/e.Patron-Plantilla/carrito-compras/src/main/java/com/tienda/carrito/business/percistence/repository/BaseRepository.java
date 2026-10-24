package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/** Repositorio base generico (igual que patronTemplate). Los repositorios concretos agregan sus @Query. */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID> extends JpaRepository<T, ID> {
}

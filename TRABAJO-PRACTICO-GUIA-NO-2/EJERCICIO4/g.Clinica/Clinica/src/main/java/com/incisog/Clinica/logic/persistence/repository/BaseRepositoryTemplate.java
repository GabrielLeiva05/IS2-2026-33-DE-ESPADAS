package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface BaseRepositoryTemplate<T extends BaseEntity> extends JpaRepository<T, String> {

    List<T> findByEliminadoFalse();
}
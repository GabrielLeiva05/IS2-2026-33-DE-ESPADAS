package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepositoryTemplate<T extends BaseEntity<ID>, ID> extends JpaRepository<T, ID> {
}
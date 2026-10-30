package com.generico.patronTemplate.business.percistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.generico.patronTemplate.business.domain.entity.BaseEntity;


@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID> extends JpaRepository<T, ID> {
}


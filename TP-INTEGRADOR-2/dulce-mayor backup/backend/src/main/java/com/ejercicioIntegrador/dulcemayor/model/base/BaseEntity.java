package com.ejercicioIntegrador.dulcemayor.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Atributos comunes a todas las entidades del diagrama de clases: id (String) y eliminado (baja logica).
 * Es la entidad base sobre la que trabajan BaseRepository y BaseService.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private boolean eliminado = false;
}

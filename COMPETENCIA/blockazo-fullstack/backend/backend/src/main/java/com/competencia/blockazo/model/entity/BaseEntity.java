package com.competencia.blockazo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Entidad base del patron Template (tomada de patronTemplate).
 * Toda entidad que quiera reutilizar el ABM generico (BaseService / BaseController)
 * debe heredar de esta clase.
 *
 * La baja es LOGICA: nunca se borra el registro, se marca eliminado = true.
 */
@MappedSuperclass
public abstract class BaseEntity<ID> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected Boolean eliminado = false;

    public abstract ID getId();
    public abstract void setId(ID id);
    public abstract Boolean isEliminado();
    public abstract void setEliminado(Boolean eliminado);
}

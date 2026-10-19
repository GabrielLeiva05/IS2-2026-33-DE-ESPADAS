package com.tienda.carrito.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Entidad base (misma idea que en patronTemplate): fija el contrato comun de todas las entidades
 * y aporta la baja logica ("eliminado").
 *
 * Diferencia puntual con patronTemplate: alla el campo "id" esta fijo como Long dentro de esta clase.
 * El diagrama de este proyecto pide "id: String", por eso el atributo "id" lo declara cada entidad
 * (UUID) y esta clase solo obliga a exponerlo con getId()/setId() tipados por el generico ID.
 */
@MappedSuperclass
public abstract class BaseEntity<ID> {

    @Column(nullable = false)
    protected Boolean eliminado = false;

    public abstract ID getId();
    public abstract void setId(ID id);
    public abstract Boolean isEliminado();
    public abstract void setEliminado(Boolean eliminado);
}

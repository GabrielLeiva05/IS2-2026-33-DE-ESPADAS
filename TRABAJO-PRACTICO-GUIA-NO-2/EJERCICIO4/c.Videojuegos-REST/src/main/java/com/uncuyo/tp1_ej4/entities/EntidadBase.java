package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Estado persistente común del dominio. Se usa una superclase mapeada para
 * mantener un único ID y el borrado lógico sin agregar una tabla compartida.
 */

@MappedSuperclass
@Getter
@Setter
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean activo = true;

    protected EntidadBase() {
    }

    protected EntidadBase(long id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public final void desactivar() {
        this.activo = false;
    }
}

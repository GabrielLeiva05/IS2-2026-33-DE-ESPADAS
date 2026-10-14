package com.incisog.Clinica.models;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    @Column(nullable = false)
    protected boolean eliminado = false;

    public abstract String getId();
    public abstract void setId(String id);
    public abstract boolean isEliminado();
    public abstract void setEliminado(boolean eliminado);
}

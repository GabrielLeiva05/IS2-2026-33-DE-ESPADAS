package com.incisog.Clinica.models;

import jakarta.persistence.Entity;

/**
 * nombre, apellido, documento, id y eliminado se heredan de Persona / BaseEntity.
 * Hoy el médico no tiene atributos propios; si mañana se agrega (ej: especialidad), va acá.
 */
@Entity
public class Medico extends Persona {

    public Medico() {
    }
}

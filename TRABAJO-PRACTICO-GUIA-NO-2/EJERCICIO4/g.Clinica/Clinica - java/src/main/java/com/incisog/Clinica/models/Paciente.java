package com.incisog.Clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 * nombre, apellido, documento, id y eliminado se heredan de Persona / BaseEntity.
 * Lo propio del paciente es su foto.
 * (Sin @JsonIgnore: la entidad ya no viaja como JSON; la API usa PacienteDTO vía PacienteAdapter.)
 */
@Entity
public class Paciente extends Persona {

    // Diagrama: Paciente *..1 FotoPaciente
    @ManyToOne
    private FotoPaciente foto;

    public Paciente() {
    }

    public FotoPaciente getFoto() { return foto; }
    public void setFoto(FotoPaciente foto) { this.foto = foto; }
}

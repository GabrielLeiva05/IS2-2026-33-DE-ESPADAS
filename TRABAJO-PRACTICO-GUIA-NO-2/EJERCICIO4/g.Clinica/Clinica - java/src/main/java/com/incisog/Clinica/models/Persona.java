package com.incisog.Clinica.models;

import jakarta.persistence.MappedSuperclass;

/**
 * Superclase de las personas del sistema (Paciente y Médico).
 * Antes, Paciente y Medico repetían nombre, apellido y documento con sus getters/setters.
 *
 * @MappedSuperclass: Persona NO es una tabla; sus atributos se copian como columnas en la
 * tabla de cada hija. Por eso las tablas "paciente" y "medico" no cambian.
 *
 * Es el "Product" del patrón Factory Method: los creators fabrican Personas.
 */
@MappedSuperclass
public abstract class Persona extends BaseEntity {

    protected String nombre;
    protected String apellido;
    protected String documento;

    // Implementación de los métodos abstractos de BaseEntity (antes repetidos en cada entidad)
    @Override public String getId() { return id; }
    @Override public void setId(String id) { this.id = id; }
    @Override public boolean isEliminado() { return eliminado; }
    @Override public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}

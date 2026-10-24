package org.example;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToMany
    @JoinTable(
            name = "persona_curso", // nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "persona_id"), // FK a Persona
            inverseJoinColumns = @JoinColumn(name = "curso_id") // FK a Curso
    )
    private Set<Curso> cursos = new HashSet<>();

    public Persona() {}

    public Persona(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Curso> getCursos() { return cursos; }

    public void setCursos(Set<Curso> cursos) { this.cursos = cursos; }

    public void agregarCurso(Curso curso) {
        this.cursos.add(curso);
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cursos=" + cursos +
                '}';
    }
}

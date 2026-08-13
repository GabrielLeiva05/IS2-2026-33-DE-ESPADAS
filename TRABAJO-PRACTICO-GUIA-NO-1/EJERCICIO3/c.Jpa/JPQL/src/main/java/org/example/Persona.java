package org.example;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@NamedQuery(
        name = "Persona.buscarPorCiudad",
        query = "SELECT p FROM Persona p JOIN p.domicilio d WHERE d.ciudad = :ciudad"
)
@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "persona_curso", // nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "persona_id"), // FK a Persona
            inverseJoinColumns = @JoinColumn(name = "curso_id") // FK a Curso
    )
    private Set<Curso> cursos = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Domicilio domicilio;


    public Persona() {}
    public Persona(String nombre) {
        this.nombre = nombre;
    }

    public void agregarCurso(Curso curso) {
        cursos.add(curso);
        curso.agregarInscriptos(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(Set<Curso> cursos) {
        this.cursos = cursos;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
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
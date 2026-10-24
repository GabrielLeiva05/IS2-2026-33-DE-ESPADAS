package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpaJPQL");
        EntityManager em = emf.createEntityManager();

        cargarDatosIniciales(em);

        // ===== Ejemplo 1: Personas ordenadas por nombre =====
        System.out.println("=== Ejemplo 1: Personas ordenadas por nombre ===");
        List<Persona> personasOrdenadas = em.createQuery(
                        "SELECT p FROM Persona p ORDER BY p.nombre ASC", Persona.class)
                .getResultList();
        personasOrdenadas.forEach(System.out::println);

        // ===== Ejemplo 2: Personas filtradas por ciudad =====
        System.out.println("\n=== Ejemplo 2: Personas filtradas por ciudad ===");
        List<Persona> personasPorCiudad = em.createQuery(
                        "SELECT p FROM Persona p JOIN p.domicilio d WHERE d.ciudad = :ciudad", Persona.class)
                .setParameter("ciudad", "Springfield")
                .getResultList();
        personasPorCiudad.forEach(System.out::println);

        // ===== Ejemplo 3: Cursos con más de N inscriptos =====
        System.out.println("\n=== Ejemplo 3: Cursos con más de N inscriptos ===");
        Long minimo = 1L;
        List<Curso> cursosPopulares = em.createQuery(
                        "SELECT c FROM Curso c WHERE SIZE(c.inscriptos) > :minimo", Curso.class)
                .setParameter("minimo", minimo)
                .getResultList();
        cursosPopulares.forEach(c -> System.out.println(c.getNombre() + " → inscriptos: " + c.getInscriptos().size()));

        // ===== Ejemplo 4: Uso de NamedQuery =====
        System.out.println("\n=== Ejemplo 4: Personas por ciudad (NamedQuery) ===");
        List<Persona> personasSpringfield = em.createNamedQuery("Persona.buscarPorCiudad", Persona.class)
                .setParameter("ciudad", "Springfield")
                .getResultList();

        personasSpringfield.forEach(System.out::println);

        em.close();
        emf.close();
    }

    private static void cargarDatosIniciales(EntityManager em) {
        em.getTransaction().begin();

        // Domicilios
        Domicilio d1 = new Domicilio("Av. Siempre Viva 742", "Springfield");
        Domicilio d2 = new Domicilio("Calle Falsa 123", "Shelbyville");

        // Cursos
        Curso java = new Curso("Java Básico");
        Curso bd = new Curso("Base de Datos");
        Curso spring = new Curso("Spring Boot");

        // Personas
        Persona p1 = new Persona("Homero");
        p1.setDomicilio(d1);
        p1.agregarCurso(java);
        p1.agregarCurso(bd);

        Persona p2 = new Persona("Marge");
        p2.setDomicilio(d2);
        p2.agregarCurso(spring);
        p2.agregarCurso(bd);


        em.persist(p1);
        em.persist(p2);

        em.getTransaction().commit();
    }
}
package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("manyToManyUni");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            Curso curso1 = new Curso("Java Básico");
            Curso curso2 = new Curso("Base de Datos");
            em.persist(curso2);
            em.persist(curso1);


            Persona persona1 = new Persona("Lucía");
            persona1.agregarCurso(curso1);
            persona1.agregarCurso(curso2);
            em.persist(persona1);
            tx.commit();

            System.out.println("¡Persona y cursos persistidos en memoria!");

            // Mostrar
            System.out.println(em.find(Persona.class,1L));
        }catch(Exception e){
            if (tx.isActive()) {
                tx.rollback();
                System.out.println("Transacción revertida por error.");
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();

        }
    }
}
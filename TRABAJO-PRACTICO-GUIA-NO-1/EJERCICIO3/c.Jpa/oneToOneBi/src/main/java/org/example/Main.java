package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("unoAunoBi");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            // Crear domicilio
            Domicilio domicilio = new Domicilio();
            domicilio.setCalle("Wallaby Way 42");
            domicilio.setCiudad("Sydney");
            em.persist(domicilio);

            // Crear persona y asociar domicilio
            Persona persona = new Persona();
            persona.setNombre("Dory");
            persona.setDomicilio(domicilio); // Relación bidireccional
            em.persist(persona);


            tx.commit();
            // Buscar y mostrar
            Persona encontrada = em.find(Persona.class,1L);
            if (encontrada != null) {
                System.out.println(encontrada);

            } else {
                System.out.println("Persona no encontrada.");
            }

            System.out.println(" -------------- MUESTRO LA BIDIRECCIONALIDAD");

            Domicilio encontradoDom = em.find(Domicilio.class,1L);
            if (encontradoDom != null) {
                System.out.println(encontradoDom.getPersona());
                //System.out.println("Domicilio: " + encontradoDom.getCalle());
            } else {
                System.out.println("Persona no encontrada.");
            }

        }catch (Exception e){
            if (tx.isActive()) {
                tx.rollback();
                System.out.println("Transacción revertida por error.");
            }
            e.printStackTrace();
        }finally {
            em.close();
            emf.close();
        }

    }
}
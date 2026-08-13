package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("oneToManyUni");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Crear domicilios
            Domicilio d1 = new Domicilio();
            d1.setCalle("Av. Siempre Viva 742");
            d1.setCiudad("Springfield");
            em.persist(d1);

            Domicilio d2 = new Domicilio();
            d2.setCalle("Calle Falsa 123");
            d2.setCiudad("Shelbyville");
            em.persist(d2);

            // Crear persona
            Persona persona = new Persona();
            persona.setNombre("Homero Simpson");
            persona.addDomicilio(d1);
            persona.addDomicilio(d2);
            em.persist(persona);
            tx.commit();
            // Buscar y mostrar
            Persona encontrada = em.find(Persona.class, persona.getId());
            if (encontrada != null) {
                System.out.println("Persona: " + encontrada.getNombre());
                for (Domicilio d : encontrada.getDomicilios()) {
                    System.out.println("Domicilio: " + d.getCalle() + ", " + d.getCiudad());
                }
            } else {
                System.out.println("Persona no encontrada.");
            }

            //Editamos un domicilio
            Domicilio domEncontrado= em.find(Domicilio.class,1L);
            domEncontrado.setCiudad("Mar del Plata");



            //Eliminamos un domicilio
            Domicilio domEncontrado2= em.find(Domicilio.class,2L);
            persona.getDomicilios().remove(domEncontrado2);
            em.remove(domEncontrado2);
            tx.begin();
            tx.commit();

            //Imprimimos una persona despues de borrar domicilio
            Persona personaEncontrada= em.find(Persona.class,1L);
            System.out.println("Persona sin 1 domicilio: "+personaEncontrada);

        } catch (Exception e) {
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
package org.example;


import jakarta.persistence.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("unoAunoUni");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            //Creamos un domicilio
            Domicilio domicilio = new Domicilio();
            domicilio.setCalle("Av. Siempre Viva 742");
            domicilio.setCiudad("Springfield");

            em.persist(domicilio);

            //Creamos un domicilio
            Domicilio domicilio2 = new Domicilio();
            domicilio2.setCalle("Av. Siempre Viva 742");
            domicilio2.setCiudad("Springfield");


            em.persist(domicilio2);

            //Creamos una Persona
            Persona persona = new Persona();
            persona.setNombre("Homero Simpson");
            persona.setDomicilio(domicilio);
            em.persist(persona);

            //Creamos una Persona
            Persona persona2 = new Persona();
            persona2.setNombre("Marge Simpson");
            persona2.setDomicilio(domicilio2);
            em.persist(persona2);

            Persona personaBuscada = em.find(Persona.class, 1L);
            System.out.println(personaBuscada);
            tx.commit();


            System.out.println("El id de domicilio es: " + domicilio.getId());
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
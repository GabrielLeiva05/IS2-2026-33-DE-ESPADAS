package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ejemploLibroAutor");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Autor autor1 = new Autor();
            autor1.setNombre("David Luis");

            Autor autor2 = new Autor();
            autor2.setNombre("Karl Marx");

            Libro libro1 = new Libro("Como ser un buen comunista", autor2);
            Libro libro2 = new Libro("En busca de la felicidad", autor1);
            Libro libro3 = new Libro("Las siete puertas del infierno de Mendoza", autor1);
            em.persist(autor1);
            em.persist(autor2);
            tx.commit();

            System.out.println("¡Autores y Libros persistidos en memoria!");

            //luego los eliminamos
            //em.remove(autor1);

            em.find(Libro.class, 2L);
        }catch(Exception e){
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
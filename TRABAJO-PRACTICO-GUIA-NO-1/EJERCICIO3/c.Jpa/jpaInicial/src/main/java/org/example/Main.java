package org.example;

import entidades.Persona;
import entidades.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cicloVidaJPA");
        EntityManager em = emf.createEntityManager();

        // Estado Transient
        Producto producto = new Producto("Playstation 2", 2000.0);


        //De Transient a Persist
        em.getTransaction().begin();
        em.persist(producto);
        em.getTransaction().commit();
        System.out.println("¿Que paso aca?: " + producto);

        //Ahora modificamos eso hijo
        em.getTransaction().begin();
        producto.setNombre("Gamestation 3");
        em.getTransaction().commit();
        System.out.println("Changes are good: " + producto);

        // ===== De GESTIONADO a DESASOCIADO (Detached) =====
        em.close(); // cerramos el EntityManager
        System.out.println("Estado DESASOCIADO (Detached): " + producto);

        // ===== Cambios en Detached (NO se guardan) =====
        producto.setPrecio(999.0);
        System.out.println("Cambio en Detached (NO se guarda): " + producto);

        // ===== De DESASOCIADO a GESTIONADO (merge) =====
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Producto productoGestionado = em2.merge(producto); // vuelve a gestionado
        em2.getTransaction().commit();
        System.out.println("Estado GESTIONADO otra vez (con merge): " + productoGestionado);

        // ===== Estado ELIMINADO (Removed) =====
        em2.getTransaction().begin();
        em2.remove(productoGestionado); // marcado para eliminar
        em2.getTransaction().commit();
        System.out.println("Estado ELIMINADO (Removed): producto eliminado de la base");

        em2.close();
        emf.close();
    }
}
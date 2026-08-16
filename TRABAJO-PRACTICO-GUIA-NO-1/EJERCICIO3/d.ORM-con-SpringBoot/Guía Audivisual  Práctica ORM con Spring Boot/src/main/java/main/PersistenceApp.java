package main;

import java.util.Locale;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.github.javafaker.Faker;

import entidades.Articulo;
import entidades.Cliente;

public class PersistenceApp {

    private static final int TOTAL_REGISTROS = 50000;
    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceAppPU");
        EntityManager em = emf.createEntityManager();

        // Instancia Faker configurada en español
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();

        try {
            long inicio = System.currentTimeMillis();
            System.out.println("Iniciando inserción masiva con Java Faker...");

            em.getTransaction().begin();

            // 1. Inserción de 50.000 Clientes
            System.out.println("Insertando 50.000 Clientes...");
            for (int i = 1; i <= TOTAL_REGISTROS; i++) {
                String nombre = faker.name().firstName();
                String apellido = faker.name().lastName();
                int dni = 10000000 + i; // Base secuencial para garantizar la unicidad del DNI

                Cliente cliente = new Cliente(nombre, apellido, dni);
                em.persist(cliente);

                if (i % BATCH_SIZE == 0) {
                    em.flush();
                    em.clear();
                    System.out.println("Clientes procesados: " + i);
                }
            }

            // 2. Inserción de 50.000 Artículos
            System.out.println("Insertando 50.000 Artículos...");
            for (int i = 1; i <= TOTAL_REGISTROS; i++) {
                // Combina marca/comercio y nombre de producto para variedad
                String denominacion = faker.commerce().productName() + " (" + faker.company().name() + ")";
                
                // Truncar por seguridad si la columna de BD tiene límite estándar de 255 caracteres
                if (denominacion.length() > 250) {
                    denominacion = denominacion.substring(0, 250);
                }

                int precio = random.nextInt(15000) + 100; // Entre $100 y $15100
                int cantidad = random.nextInt(100) + 1;   // Entre 1 y 100 unidades

                Articulo articulo = new Articulo(denominacion, precio, cantidad);
                em.persist(articulo);

                if (i % BATCH_SIZE == 0) {
                    em.flush();
                    em.clear();
                    System.out.println("Artículos procesados: " + i);
                }
            }

            em.getTransaction().commit();
            long fin = System.currentTimeMillis();
            System.out.println("Proceso finalizado con éxito en " + ((fin - inicio) / 1000.0) + " segundos.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
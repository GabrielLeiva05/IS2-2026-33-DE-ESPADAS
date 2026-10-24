package ar.com.biblioteca.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación SERVIDOR.
 * Expone la API REST (puerto 8080) que consume la aplicación cliente.
 */
@SpringBootApplication
@EnableScheduling
public class BibliotecaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaServerApplication.class, args);
    }
}

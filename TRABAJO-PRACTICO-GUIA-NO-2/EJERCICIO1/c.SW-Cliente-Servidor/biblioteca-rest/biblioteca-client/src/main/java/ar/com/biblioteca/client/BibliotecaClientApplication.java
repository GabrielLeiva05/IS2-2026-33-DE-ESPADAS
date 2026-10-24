package ar.com.biblioteca.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación CLIENTE (sitio HTML en el puerto 8081).
 * No tiene base de datos: todo el acceso a datos se hace consumiendo la API REST del servidor con RestTemplate.
 */
@SpringBootApplication
public class BibliotecaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaClientApplication.class, args);
    }
}

package com.tienda.carrito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarritoApplication {

    public static void main(String[] args) {
        // El generador de imagenes de ejemplo usa java.awt: se fuerza modo "headless" (sin pantalla)
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(CarritoApplication.class, args);
    }
}

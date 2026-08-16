package com.laboratorio.ejerciciog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ejerciciogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ejerciciogApplication.class, args);

//        # POST - crear producto
//        Invoke-RestMethod -Uri "http://localhost:8090/api/productos" -Method Post -ContentType "application/json" -Body '{"nombre":"Auriculares","precio":25000}'
//
//# PUT - actualizar producto
//        Invoke-RestMethod -Uri "http://localhost:8090/api/productos/1" -Method Put -ContentType "application/json" -Body '{"nombre":"Teclado gamer","precio":60000}'
//
//# DELETE
//        Invoke-RestMethod -Uri "http://localhost:8090/api/productos/1" -Method Delete
//
//# GET (findAll)
//        Invoke-RestMethod -Uri "http://localhost:8090/api/productos" -Method Get
    }

}
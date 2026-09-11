package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Contacto;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContacto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contactos")
public class ContactoControlador {

    private final ServicioContacto contactoServicio;

    public ContactoControlador(ServicioContacto contactoServicio) {
        this.contactoServicio = contactoServicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarContacto(@PathVariable String id) {
        try {
            Contacto contacto = contactoServicio.buscarContacto(id);
            return ResponseEntity.ok(contacto);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarContacto(@PathVariable String id) {
        try {
            contactoServicio.eliminarContacto(id);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

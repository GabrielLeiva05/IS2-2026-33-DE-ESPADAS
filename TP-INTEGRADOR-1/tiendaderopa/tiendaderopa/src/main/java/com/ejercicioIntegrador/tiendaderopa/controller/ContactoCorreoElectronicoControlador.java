package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoCorreoElectronico;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContactoCorreoElectronico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contactos/correo")
public class ContactoCorreoElectronicoControlador {

    private final ServicioContactoCorreoElectronico servicio;

    public ContactoCorreoElectronicoControlador(ServicioContactoCorreoElectronico servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ContactoCorreoElectronico> listar() {
        return servicio.listarContactoCorreoElectronico();
    }

    @GetMapping("/activos")
    public List<ContactoCorreoElectronico> listarActivos() {
        return servicio.listarContactoCorreoElectronicoActivo();
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String email,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            @RequestParam String personaId
    ) {
        try {
            ContactoCorreoElectronico contacto = servicio.crearContactoCorreoElectronico(email, tipoContacto, observacion, personaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(contacto);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String email,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion
    ) {
        try {
            ContactoCorreoElectronico contacto = servicio.modificarContactoCorreoElectronico(id, email, tipoContacto, observacion);
            return ResponseEntity.ok(contacto);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

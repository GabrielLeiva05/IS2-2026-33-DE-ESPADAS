package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contactos/telefono")
public class ContactoTelefonicoControlador {

    private final ServicioContactoTelefonico servicio;

    public ContactoTelefonicoControlador(ServicioContactoTelefonico servicio, ServicioProveedor servicioProveedor) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ContactoTelefonico> listar() {
        return servicio.listarContactoTelefonico();
    }

    @GetMapping("/activos")
    public List<ContactoTelefonico> listarActivos() {
        return servicio.listarContactoTelefonicoActivo();
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String telefono,
            @RequestParam TipoTelefono tipoTelefono,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            @RequestParam(required = false) String personaId,
            @RequestParam(required = false) String proveedorId
    ) {
        try {
            // El Controller ya no decide nada ni instancia nada: solo pasa
            // los datos tal cual llegaron. Validar cuál es válido y resolver
            // las entidades es responsabilidad exclusiva del Service.
            ContactoTelefonico contacto = servicio.crearContactoTelefonico(
                    telefono, tipoTelefono, tipoContacto, observacion, personaId, proveedorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(contacto);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String telefono,
            @RequestParam TipoTelefono tipoTelefono,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion
    ) {
        try {
            ContactoTelefonico contacto = servicio.modificarContactoTelefonico(id, telefono, tipoTelefono, tipoContacto, observacion);
            return ResponseEntity.ok(contacto);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empleado;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioEmpleado;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/empleados")
public class EmpleadoControlador {

    private final ServicioEmpleado servicio;

    public EmpleadoControlador(ServicioEmpleado servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Empleado> listar() {
        return servicio.listarEmpleado();
    }

    @GetMapping("/activos")
    public List<Empleado> listarActivos() {
        return servicio.listarEmpleadoActivo();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        try {
            Empleado empleado = servicio.buscarEmpleado(id);
            return ResponseEntity.ok(empleado);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            @RequestParam TipoEmpleado tipoEmpleado,
            @RequestParam(required = false) String idEmpresa
    ) {
        try {
            Empleado empleado = servicio.crearEmpleado(nombre, apellido, fechaNacimiento, tipoDocumento,
                    documento, tipoEmpleado, idEmpresa);
            return ResponseEntity.status(HttpStatus.CREATED).body(empleado);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            @RequestParam TipoEmpleado tipoEmpleado,
            @RequestParam(required = false) String idEmpresa
    ) {
        try {
            Empleado empleado = servicio.modificarEmpleado(id, nombre, apellido, fechaNacimiento, tipoDocumento,
                    documento, tipoEmpleado, idEmpresa);
            return ResponseEntity.ok(empleado);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            servicio.eliminarEmpleado(id);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{idEmpleado}/usuario/{idUsuario}")
    public ResponseEntity<?> asociarUsuario(@PathVariable String idEmpleado, @PathVariable String idUsuario) {
        try {
            servicio.asociarEmpleadoUsuario(idEmpleado, idUsuario);
            return ResponseEntity.ok().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
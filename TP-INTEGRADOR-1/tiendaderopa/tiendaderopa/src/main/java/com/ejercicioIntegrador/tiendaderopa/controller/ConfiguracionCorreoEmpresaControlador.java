package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ConfiguracionCorreoEmpresa;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioConfiguracionCorreoEmpresa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configuraciones-correo")
public class ConfiguracionCorreoEmpresaControlador {

    private final ServicioConfiguracionCorreoEmpresa servicio;

    public ConfiguracionCorreoEmpresaControlador(ServicioConfiguracionCorreoEmpresa servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ConfiguracionCorreoEmpresa> listar() {
        return servicio.listarConfiguracionCorreoAutomatico();
    }

    @GetMapping("/activas")
    public List<ConfiguracionCorreoEmpresa> listarActivas() {
        return servicio.listarConfiguracionCorreoAutomaticoActiva();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        try {
            ConfiguracionCorreoEmpresa configuracion = servicio.buscarConfiguracionCorreoAutomatico(id);
            return ResponseEntity.ok(configuracion);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String correo,
            @RequestParam String clave,
            @RequestParam String puerto,
            @RequestParam String smtp,
            @RequestParam boolean tls,
            @RequestParam String idEmpresa
    ) {
        try {
            ConfiguracionCorreoEmpresa configuracion =
                    servicio.crearConfiguracionCorreoAutomatico(correo, clave, puerto, smtp, tls, idEmpresa);
            return ResponseEntity.status(HttpStatus.CREATED).body(configuracion);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String correo,
            @RequestParam String clave,
            @RequestParam String puerto,
            @RequestParam String smtp,
            @RequestParam boolean tls,
            @RequestParam String idEmpresa
    ) {
        try {
            ConfiguracionCorreoEmpresa configuracion =
                    servicio.modificarConfiguracionCorreoAutomatico(id, correo, clave, puerto, smtp, tls, idEmpresa);
            return ResponseEntity.ok(configuracion);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            servicio.eliminarConfiguracionCorreoAutomatico(id);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarCorreo(
            @PathVariable String id,
            @RequestParam String destinatario,
            @RequestParam String asunto,
            @RequestParam String cuerpoHtml
    ) {
        try {
            servicio.enviarCorreo(id, destinatario, asunto, cuerpoHtml);
            return ResponseEntity.ok().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
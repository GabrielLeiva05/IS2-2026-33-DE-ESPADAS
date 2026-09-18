package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoSucursal;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioEmpresa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
public class EmpresaControlador {

    private final ServicioEmpresa servicio;

    public EmpresaControlador(ServicioEmpresa servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Empresa> listar() {
        return servicio.listarEmpresa();
    }

    @GetMapping("/activas")
    public List<Empresa> listarActivas() {
        return servicio.listarEmpresaActiva();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        try {
            Empresa empresa = servicio.buscarEmpresa(id);
            return ResponseEntity.ok(empresa);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String razonSocial) {
        try {
            Empresa empresa = servicio.buscarEmpresaPorNombre(razonSocial);
            return ResponseEntity.ok(empresa);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String razonSocial,
            @RequestParam String cuit,
            @RequestParam TipoSucursal tipoSucursal
    ) {
        try {
            Empresa empresa = servicio.crearEmpresa(razonSocial, cuit, tipoSucursal);
            return ResponseEntity.status(HttpStatus.CREATED).body(empresa);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String razonSocial,
            @RequestParam String cuit,
            @RequestParam TipoSucursal tipoSucursal
    ) {
        try {
            Empresa empresa = servicio.modificarEmpresa(id, razonSocial, cuit, tipoSucursal);
            return ResponseEntity.ok(empresa);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            servicio.eliminarEmpresa(id);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
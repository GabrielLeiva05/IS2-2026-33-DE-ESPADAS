package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioLocalidad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
public class ControladorLocalidad extends BaseController<LocalidadRequestDTO, LocalidadResponseDTO> {

    private final ServicioLocalidad servicioLocalidad;

    public ControladorLocalidad(ServicioLocalidad servicioLocalidad) {
        super(servicioLocalidad);
        this.servicioLocalidad = servicioLocalidad;
    }

    @GetMapping("/buscar")
    public ResponseEntity<LocalidadResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioLocalidad.buscarLocalidadPorNombre(nombre));
    }

    /** Activas del departamento indicado (para los selects en cascada). */
    @GetMapping("/departamento/{idDepartamento}")
    public ResponseEntity<List<LocalidadResponseDTO>> listarActivas(@PathVariable String idDepartamento) throws MiException {
        return ResponseEntity.ok(servicioLocalidad.listarLocalidadActivo(idDepartamento));
    }

    @GetMapping("/departamento/{idDepartamento}/todas")
    public ResponseEntity<List<LocalidadResponseDTO>> listarTodas(@PathVariable String idDepartamento) throws MiException {
        return ResponseEntity.ok(servicioLocalidad.listarLocalidad(idDepartamento));
    }

    @GetMapping("/codigo-postal/{codigoPostal}")
    public ResponseEntity<LocalidadResponseDTO> buscarPorCodigoPostal(@PathVariable String codigoPostal) throws MiException {
        return ResponseEntity.ok(servicioLocalidad.buscarLocalidadPorCodigoPostal(codigoPostal));
    }
}

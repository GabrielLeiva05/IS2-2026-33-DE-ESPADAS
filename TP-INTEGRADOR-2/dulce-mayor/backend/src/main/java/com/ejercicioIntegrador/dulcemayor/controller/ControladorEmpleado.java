package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de empleados (HU-07). Hereda los endpoints del Template Method; el acceso está
 * restringido al rol JEFE en SecurityConfig.
 */
@RestController
@RequestMapping("/api/empleados")
public class ControladorEmpleado extends BaseController<EmpleadoRequestDTO, EmpleadoResponseDTO> {

    private final ServicioEmpleado servicioEmpleado;

    public ControladorEmpleado(ServicioEmpleado servicioEmpleado) {
        super(servicioEmpleado);
        this.servicioEmpleado = servicioEmpleado;
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarPorRol(@PathVariable RolUsuario rol) throws MiException {
        return ResponseEntity.ok(servicioEmpleado.listarPorRol(rol));
    }

    @GetMapping("/legajo/{legajo}")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorLegajo(@PathVariable String legajo) throws MiException {
        return ResponseEntity.ok(servicioEmpleado.buscarEmpleadoPorLegajo(legajo));
    }
}

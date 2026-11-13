package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioDepartamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class ControladorDepartamento extends BaseController<DepartamentoRequestDTO, DepartamentoResponseDTO> {

    private final ServicioDepartamento servicioDepartamento;

    public ControladorDepartamento(ServicioDepartamento servicioDepartamento) {
        super(servicioDepartamento);
        this.servicioDepartamento = servicioDepartamento;
    }

    @GetMapping("/buscar")
    public ResponseEntity<DepartamentoResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioDepartamento.buscarDepartamentoPorNombre(nombre));
    }

    /** Activas del provincia indicado (para los selects en cascada). */
    @GetMapping("/provincia/{idProvincia}")
    public ResponseEntity<List<DepartamentoResponseDTO>> listarActivas(@PathVariable String idProvincia) throws MiException {
        return ResponseEntity.ok(servicioDepartamento.listarDepartamentoActivo(idProvincia));
    }

    @GetMapping("/provincia/{idProvincia}/todas")
    public ResponseEntity<List<DepartamentoResponseDTO>> listarTodas(@PathVariable String idProvincia) throws MiException {
        return ResponseEntity.ok(servicioDepartamento.listarDepartamento(idProvincia));
    }
}

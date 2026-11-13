package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProvincia;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provincias")
public class ControladorProvincia extends BaseController<ProvinciaRequestDTO, ProvinciaResponseDTO> {

    private final ServicioProvincia servicioProvincia;

    public ControladorProvincia(ServicioProvincia servicioProvincia) {
        super(servicioProvincia);
        this.servicioProvincia = servicioProvincia;
    }

    @GetMapping("/buscar")
    public ResponseEntity<ProvinciaResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioProvincia.buscarProvinciaPorNombre(nombre));
    }

    /** Activas del país indicado (para los selects en cascada). */
    @GetMapping("/pais/{idPais}")
    public ResponseEntity<List<ProvinciaResponseDTO>> listarActivas(@PathVariable String idPais) throws MiException {
        return ResponseEntity.ok(servicioProvincia.listarProvinciaActivo(idPais));
    }

    @GetMapping("/pais/{idPais}/todas")
    public ResponseEntity<List<ProvinciaResponseDTO>> listarTodas(@PathVariable String idPais) throws MiException {
        return ResponseEntity.ok(servicioProvincia.listarProvincia(idPais));
    }
}

package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.PaisRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PaisResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPais;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paises")
public class ControladorPais extends BaseController<PaisRequestDTO, PaisResponseDTO> {

    private final ServicioPais servicioPais;

    public ControladorPais(ServicioPais servicioPais) {
        super(servicioPais);
        this.servicioPais = servicioPais;
    }

    @GetMapping("/buscar")
    public ResponseEntity<PaisResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioPais.buscarPaisPorNombre(nombre));
    }
}

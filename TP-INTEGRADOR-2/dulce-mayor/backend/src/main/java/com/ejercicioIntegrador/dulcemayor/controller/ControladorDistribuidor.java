package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.CerrarParadaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReordenarRutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioRuta;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Hoja de ruta del distribuidor autenticado: solo puede operar su propia jornada. */
@RestController
@RequestMapping("/api/distribuidor")
public class ControladorDistribuidor {

    private final ServicioRuta servicioRuta;

    public ControladorDistribuidor(ServicioRuta servicioRuta) {
        this.servicioRuta = servicioRuta;
    }

    @GetMapping("/ruta")
    public ResponseEntity<RutaResponseDTO> miRuta(Authentication authentication) throws MiException {
        return ResponseEntity.of(servicioRuta.miRuta(authentication.getName()));
    }

    @GetMapping("/ruta/{id}")
    public ResponseEntity<RutaResponseDTO> detalle(@PathVariable String id, Authentication authentication)
            throws MiException {
        return ResponseEntity.ok(servicioRuta.detalleDelDistribuidor(id, authentication.getName()));
    }

    @PutMapping("/ruta/{id}/orden")
    public ResponseEntity<RutaResponseDTO> reordenar(@PathVariable String id,
                                                      @RequestBody ReordenarRutaRequestDTO dto,
                                                      Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioRuta.reordenar(id, dto, authentication.getName()));
    }

    @PostMapping("/paradas/{id}/entregar")
    public ResponseEntity<RutaResponseDTO> entregar(@PathVariable String id,
                                                     @RequestBody(required = false) CerrarParadaRequestDTO dto,
                                                     Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioRuta.entregar(id, dto, authentication.getName()));
    }

    @PostMapping("/paradas/{id}/fallar")
    public ResponseEntity<RutaResponseDTO> fallar(@PathVariable String id,
                                                   @RequestBody CerrarParadaRequestDTO dto,
                                                   Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioRuta.fallar(id, dto, authentication.getName()));
    }
}

package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.GeocodificacionDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioMapas;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Geocodificación para el alta de puntos de venta (HU-17). La consulta a Nominatim la hace el backend. */
@RestController
@RequestMapping("/api/mapas")
public class ControladorMapas {

    private final ServicioMapas servicioMapas;

    public ControladorMapas(ServicioMapas servicioMapas) {
        this.servicioMapas = servicioMapas;
    }

    @GetMapping("/geocodificar")
    public ResponseEntity<List<GeocodificacionDTO>> geocodificar(@RequestParam String direccion) throws MiException {
        return ResponseEntity.ok(servicioMapas.geocodificar(direccion));
    }

    @GetMapping("/geocodificacion-inversa")
    public ResponseEntity<GeocodificacionDTO> inversa(@RequestParam Double lat, @RequestParam Double lon)
            throws MiException {
        return ResponseEntity.ok(servicioMapas.geocodificarInversa(lat, lon).orElseThrow(() ->
                new RecursoNoEncontradoException("No se encontró una dirección para ese punto: completala a mano")));
    }
}

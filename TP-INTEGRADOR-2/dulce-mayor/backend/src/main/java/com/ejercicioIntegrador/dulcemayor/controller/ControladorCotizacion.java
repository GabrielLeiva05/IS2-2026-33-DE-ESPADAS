package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.CotizacionDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.ServicioExternoNoDisponibleException;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ServicioCotizacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cotización del dólar que usa el sistema (pública: la muestra el sitio institucional). */
@RestController
@RequestMapping("/api/cotizacion")
public class ControladorCotizacion {

    private final ServicioCotizacion servicioCotizacion;

    public ControladorCotizacion(ServicioCotizacion servicioCotizacion) {
        this.servicioCotizacion = servicioCotizacion;
    }

    @GetMapping
    public ResponseEntity<CotizacionDTO> actual() throws ServicioExternoNoDisponibleException {
        return ResponseEntity.ok(servicioCotizacion.actual().orElseThrow(() ->
                new ServicioExternoNoDisponibleException("La cotización del dólar no está disponible en este momento")));
    }
}

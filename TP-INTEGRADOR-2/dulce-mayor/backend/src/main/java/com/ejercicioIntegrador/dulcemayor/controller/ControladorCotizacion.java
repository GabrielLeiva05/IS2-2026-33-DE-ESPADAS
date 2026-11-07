package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.CotizacionDolarDTO;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCotizacionDolar;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cotización informativa del dólar (BCRA). Nunca bloquea catálogo ni pedidos. */
@RestController
@RequestMapping("/api/cotizacion-dolar")
public class ControladorCotizacion {

    private final ServicioCotizacionDolar servicioCotizacion;

    public ControladorCotizacion(ServicioCotizacionDolar servicioCotizacion) {
        this.servicioCotizacion = servicioCotizacion;
    }

    @GetMapping
    public ResponseEntity<CotizacionDolarDTO> actual() {
        return ResponseEntity.ok(servicioCotizacion.obtener());
    }
}

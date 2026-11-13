package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.MensajePresupuestoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RespuestaChatDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.presupuesto.ServicioPresupuesto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Asistente de presupuestos con IA (solo clientes registrados; ver SecurityConfig). */
@RestController
@RequestMapping("/api/presupuestos/chat")
public class ControladorPresupuesto {

    private final ServicioPresupuesto servicioPresupuesto;

    public ControladorPresupuesto(ServicioPresupuesto servicioPresupuesto) {
        this.servicioPresupuesto = servicioPresupuesto;
    }

    @PostMapping
    public ResponseEntity<RespuestaChatDTO> conversar(Authentication autenticacion,
                                                      @RequestBody MensajePresupuestoDTO mensaje) throws MiException {
        return ResponseEntity.ok(servicioPresupuesto.conversar(autenticacion.getName(), mensaje));
    }

    @DeleteMapping("/{idConversacion}")
    public ResponseEntity<Void> cerrar(Authentication autenticacion, @PathVariable String idConversacion) {
        servicioPresupuesto.cerrar(autenticacion.getName(), idConversacion);
        return ResponseEntity.noContent().build();
    }
}

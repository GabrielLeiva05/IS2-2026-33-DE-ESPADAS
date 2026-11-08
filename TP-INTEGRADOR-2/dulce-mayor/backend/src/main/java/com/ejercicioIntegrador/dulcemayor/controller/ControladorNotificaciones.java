package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.model.EnvioCorreo;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCorreo;
import com.ejercicioIntegrador.dulcemayor.service.ServicioNewsletter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Consulta del registro de correos y disparo manual del newsletter (administración). */
@RestController
@RequestMapping("/api/notificaciones")
public class ControladorNotificaciones {

    private final ServicioCorreo servicioCorreo;
    private final ServicioNewsletter servicioNewsletter;

    public ControladorNotificaciones(ServicioCorreo servicioCorreo, ServicioNewsletter servicioNewsletter) {
        this.servicioCorreo = servicioCorreo;
        this.servicioNewsletter = servicioNewsletter;
    }

    @GetMapping("/correos")
    public ResponseEntity<List<EnvioCorreo>> correos() {
        return ResponseEntity.ok(servicioCorreo.listar());
    }

    @GetMapping("/correos/{destinatario}")
    public ResponseEntity<List<EnvioCorreo>> correosDe(@PathVariable String destinatario) {
        return ResponseEntity.ok(servicioCorreo.listarPorDestinatario(destinatario));
    }

    @GetMapping("/newsletter")
    public ResponseEntity<List<com.ejercicioIntegrador.dulcemayor.dto.SuscripcionNewsletterResponseDTO>> suscripciones() {
        return ResponseEntity.ok(servicioNewsletter.listar());
    }

    /** Permite probar el envío mensual sin esperar al día programado. */
    @PostMapping("/newsletter/enviar")
    public ResponseEntity<Map<String, Object>> enviarNewsletter(@RequestParam(required = false) String contenido) {
        int enviados = servicioNewsletter.enviarEdicionMensual(contenido);
        return ResponseEntity.ok(Map.of("enviados", enviados, "smtpConfigurado", servicioCorreo.envioRealDisponible()));
    }
}

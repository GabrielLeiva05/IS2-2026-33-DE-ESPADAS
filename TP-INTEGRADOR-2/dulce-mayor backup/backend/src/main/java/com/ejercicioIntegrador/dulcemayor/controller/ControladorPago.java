package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Recepción de notificaciones (webhook) de Mercado Pago.
 * Es público: Mercado Pago no envía el JWT de la aplicación. El estado real siempre se consulta en la API de
 * Mercado Pago, así que un POST falsificado no puede acreditar un pago.
 */
@RestController
@RequestMapping("/api/pagos")
public class ControladorPago {

    private final ServicioPago servicioPago;

    public ControladorPago(ServicioPago servicioPago) {
        this.servicioPago = servicioPago;
    }

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> notificacionMercadoPago(@RequestParam Map<String, String> parametros,
                                                        HttpServletRequest request) {
        servicioPago.procesarNotificacion(parametros, leerCuerpo(request));
        return ResponseEntity.ok().build();
    }

    /** Mercado Pago puede mandar los datos como query params, formulario o JSON en el cuerpo. */
    private static String leerCuerpo(HttpServletRequest request) {
        try (var lector = request.getReader()) {
            StringBuilder cuerpo = new StringBuilder();
            char[] buffer = new char[1024];
            int leidos;
            while ((leidos = lector.read(buffer)) != -1) {
                cuerpo.append(buffer, 0, leidos);
            }
            return cuerpo.toString();
        } catch (IOException e) {
            return null;
        }
    }
}

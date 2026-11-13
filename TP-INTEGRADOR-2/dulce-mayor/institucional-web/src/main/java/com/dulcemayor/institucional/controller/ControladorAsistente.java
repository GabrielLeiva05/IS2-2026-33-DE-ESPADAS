package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.MensajeAsistenteDTO;
import com.dulcemayor.institucional.dto.RespuestaAsistenteDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.seguridad.SesionCliente;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * Puente entre el chat del sitio y /api/presupuestos/chat. El navegador nunca ve el token: se toma de la sesión del
 * servidor. Solo clientes con sesión; además se exige el encabezado X-Requested-With (lo pone el script del sitio),
 * para que otro sitio no pueda disparar mensajes con un formulario.
 */
@RestController
@RequestMapping("/asistente")
public class ControladorAsistente {

    private static final String ENCABEZADO = "X-Requested-With";

    private final ClienteApi clienteApi;
    private final GestorSesion gestorSesion;

    public ControladorAsistente(ClienteApi clienteApi, GestorSesion gestorSesion) {
        this.clienteApi = clienteApi;
        this.gestorSesion = gestorSesion;
    }

    @PostMapping(path = "/mensajes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enviar(HttpServletRequest request, @RequestHeader(value = ENCABEZADO, required = false) String origen,
                                    @RequestBody MensajeAsistenteDTO mensaje) {
        Optional<SesionCliente> sesion = sesionValida(request, origen);
        if (sesion.isEmpty()) {
            return error(401, "Ingresá con tu cuenta para usar el asistente.");
        }
        try {
            return ResponseEntity.ok(clienteApi.enviar(sesion.get().getToken(), mensaje, RespuestaAsistenteDTO.class,
                    "/api/presupuestos/chat"));
        } catch (ApiException e) {
            return error(e.getEstado(), e.getMessage());
        }
    }

    @DeleteMapping("/conversaciones/{id}")
    public ResponseEntity<?> cerrar(HttpServletRequest request, @RequestHeader(value = ENCABEZADO, required = false) String origen,
                                    @PathVariable String id) {
        Optional<SesionCliente> sesion = sesionValida(request, origen);
        if (sesion.isEmpty()) {
            return error(401, "Ingresá con tu cuenta para usar el asistente.");
        }
        try {
            clienteApi.eliminar(sesion.get().getToken(), "/api/presupuestos/chat/{id}", id);
            return ResponseEntity.noContent().build();
        } catch (ApiException e) {
            return error(e.getEstado(), e.getMessage());
        }
    }

    private Optional<SesionCliente> sesionValida(HttpServletRequest request, String origen) {
        if (!"fetch".equals(origen)) {
            return Optional.empty();
        }
        return gestorSesion.actual(request).filter(s -> !s.isDebeCambiarClave());
    }

    private static ResponseEntity<Map<String, String>> error(int estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje == null ? "No se pudo completar." : mensaje));
    }
}

package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Estado de una conversación del asistente (en memoria, por cliente; vence por inactividad). */
final class Conversacion {

    static final int TURNOS_RECORDADOS = 10;

    /** Pedido ambiguo a la espera de que el cliente elija una opción. */
    record Pendiente(AccionPresupuesto accion, Integer cantidad, List<String> idsProducto) {
    }

    private final String id = UUID.randomUUID().toString();
    private final String usuario;
    private final Map<String, Integer> items = new LinkedHashMap<>();
    private final List<MensajeChat> historial = new ArrayList<>();
    private Pendiente pendiente;
    private Instant ultimaActividad;

    Conversacion(String usuario, Instant ahora) {
        this.usuario = usuario;
        this.ultimaActividad = ahora;
    }

    String id() {
        return id;
    }

    String usuario() {
        return usuario;
    }

    Map<String, Integer> items() {
        return items;
    }

    List<MensajeChat> historial() {
        return List.copyOf(historial);
    }

    Pendiente pendiente() {
        return pendiente;
    }

    void pendiente(Pendiente pendiente) {
        this.pendiente = pendiente;
    }

    Instant ultimaActividad() {
        return ultimaActividad;
    }

    void recordar(String mensajeUsuario, String respuesta, Instant ahora) {
        historial.add(new MensajeChat(true, mensajeUsuario));
        historial.add(new MensajeChat(false, respuesta));
        while (historial.size() > TURNOS_RECORDADOS) {
            historial.remove(0);
        }
        ultimaActividad = ahora;
    }
}

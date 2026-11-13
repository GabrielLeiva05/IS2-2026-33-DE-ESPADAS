package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import java.util.List;

/**
 * Modelo de lenguaje que interpreta los mensajes del chat de presupuestos. Solo interpreta: no consulta la base,
 * no calcula precios ni elige productos.
 */
public interface ProveedorIA {

    /** false si falta la clave de la API: el asistente responde 503 sin intentar la llamada. */
    boolean configurado();

    /**
     * @param historial        turnos previos (el más viejo primero), para entender "sumá dos más" o "sacalos"
     * @param mensaje          mensaje actual del cliente
     * @param presupuestoActual resumen en texto de lo que ya tiene cargado (nombres y cantidades)
     * @throws ErrorProveedorIA si la API no responde, rechaza la llamada o devuelve algo que no se puede leer
     */
    InterpretacionIA interpretar(List<MensajeChat> historial, String mensaje, String presupuestoActual)
            throws ErrorProveedorIA;
}

package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.util.List;

/** Respuesta del asistente: texto armado por el backend, opciones para aclarar y el presupuesto actualizado. */
public record RespuestaChatDTO(String idConversacion, String mensaje, List<OpcionChatDTO> opciones,
                               PresupuestoDTO presupuesto) {
}

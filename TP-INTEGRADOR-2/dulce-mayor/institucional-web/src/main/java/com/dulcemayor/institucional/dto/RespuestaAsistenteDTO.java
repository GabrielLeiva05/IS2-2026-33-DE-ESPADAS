package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Respuesta del asistente de presupuestos. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaAsistenteDTO {
    private String idConversacion;
    private String mensaje;
    private List<OpcionAsistenteDTO> opciones = List.of();
    private PresupuestoDTO presupuesto;
}

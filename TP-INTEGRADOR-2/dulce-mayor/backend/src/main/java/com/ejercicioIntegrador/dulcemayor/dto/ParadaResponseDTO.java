package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoParada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Parada de la hoja de ruta con los datos que necesita el distribuidor en la calle. */
public record ParadaResponseDTO(String id, String idPedido, String nombreComercial, String calle, String numeracion,
                                String barrio, String localidad, Double latitud, Double longitud, int orden,
                                EstadoParada estado, String motivoFallo, String observacion,
                                LocalDateTime fechaResolucion, boolean cobroEfectivo, BigDecimal total,
                                String estadoPedido) {
}

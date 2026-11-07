package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoRuta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RutaResponseDTO(String id, LocalDate fecha, String idDistribuidor, String nombreDistribuidor,
                              String vehiculo, String patente, EstadoRuta estado, String observacion,
                              List<ParadaResponseDTO> paradas, String idSiguienteParada, BigDecimal totalCobrado,
                              long paradasPendientes) {
}

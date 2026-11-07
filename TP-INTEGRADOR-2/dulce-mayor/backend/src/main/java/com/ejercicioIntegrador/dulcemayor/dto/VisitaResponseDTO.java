package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** Parada de la planificación diaria del preventista. */
public record VisitaResponseDTO(String id, String idPuntoVenta, String nombreComercial, String calle,
                                String numeracion, String localidad, Double latitud, Double longitud, int orden,
                                String observacion) {

    public record PlanVisitasDTO(String fecha, List<VisitaResponseDTO> visitas) {
    }
}

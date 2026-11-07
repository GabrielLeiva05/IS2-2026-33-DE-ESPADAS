package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** Nuevo orden manual de la planificación de visitas. */
public record ReordenarVisitasRequestDTO(List<String> idsVisitas) {
}

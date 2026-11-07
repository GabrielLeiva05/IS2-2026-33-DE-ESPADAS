package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** Nuevo orden de las paradas elegido por el distribuidor. */
public record ReordenarRutaRequestDTO(List<String> idsParadas) {
}

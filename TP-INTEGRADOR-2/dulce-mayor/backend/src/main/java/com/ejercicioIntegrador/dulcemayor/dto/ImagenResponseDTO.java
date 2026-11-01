package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;

/** Metadatos de la imagen. El contenido se descarga en GET /api/imagenes/{id}/contenido. */
public record ImagenResponseDTO(String id, String nombre, String mime, TipoImagen tipoImagen, long tamanioBytes,
                                boolean eliminado) implements BaseResponseDTO {
}

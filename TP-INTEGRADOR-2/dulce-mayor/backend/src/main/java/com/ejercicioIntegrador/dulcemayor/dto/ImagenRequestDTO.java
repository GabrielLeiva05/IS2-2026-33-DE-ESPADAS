package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;

/**
 * Datos de entrada de una imagen. En JSON, "contenido" viaja en Base64.
 * Cuando se sube un archivo (multipart), el controlador arma este DTO a partir del archivo.
 */
public record ImagenRequestDTO(String nombre, String mime, byte[] contenido, TipoImagen tipoImagen) {
}

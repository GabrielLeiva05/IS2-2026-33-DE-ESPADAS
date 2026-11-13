package com.ejercicioIntegrador.dulcemayor.dto;

/** Contenido binario de una imagen, para devolverla al navegador. */
public record ImagenContenidoDTO(String nombre, String mime, byte[] contenido) {
}

package com.tienda.carrito.dto;

/** Archivo de imagen ya leido por el controlador. Asi el servicio no depende de clases web (MultipartFile). */
public record ArchivoImagen(String nombre, String mime, byte[] contenido) { }

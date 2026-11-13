package com.dulcemayor.admin.dto;

/** Archivo que generó la API (PDF o Excel) y que admin-web entrega al navegador sin modificarlo. */
public record ArchivoDescargaDTO(String nombre, String tipoContenido, byte[] contenido) {
}

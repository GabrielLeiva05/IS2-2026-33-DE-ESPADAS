package com.ejercicioIntegrador.dulcemayor.controller.base;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Respuesta HTTP de descarga de un PDF (application/pdf + Content-Disposition: attachment). */
public final class RespuestaPdf {

    private RespuestaPdf() {
    }

    public static ResponseEntity<byte[]> de(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }
}

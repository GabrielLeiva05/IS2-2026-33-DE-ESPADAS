package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Foto de un producto (id y nombre); el contenido se pide a /api/imagenes/{id}/contenido. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImagenProductoDTO {

    private String id;
    private String nombre;
}

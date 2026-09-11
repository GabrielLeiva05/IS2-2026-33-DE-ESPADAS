package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Imagen;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioImagen;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/imagen")
public class ImagenControlador {

    private final ServicioImagen imagenServicio;

    public ImagenControlador(ServicioImagen imagenServicio) {
        this.imagenServicio = imagenServicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String id) {

        Imagen imagen = imagenServicio.findById(id);

        if (imagen == null || imagen.getContenido() == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(imagen.getMime())
                )
                .body(imagen.getContenido());
    }
}

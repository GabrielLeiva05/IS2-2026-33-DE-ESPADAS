package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Imagen;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioImagen;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping
    public ResponseEntity<?> crearImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoImagen") TipoImagen tipoImagen
    ) {
        try {
            Imagen imagen = imagenServicio.guardar(archivo, tipoImagen);
            return ResponseEntity.status(HttpStatus.CREATED).body(imagen);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarImagen(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoImagen") TipoImagen tipoImagen
    ) {
        try {
            Imagen imagen = imagenServicio.actualizar(archivo, id, tipoImagen);
            return ResponseEntity.ok(imagen);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
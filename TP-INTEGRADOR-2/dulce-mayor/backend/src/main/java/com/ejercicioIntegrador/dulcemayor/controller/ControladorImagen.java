package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenContenidoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioImagen;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Además de los endpoints JSON heredados (contenido en Base64), permite subir el archivo
 * como multipart/form-data, que es lo que usan los formularios de las apps cliente.
 */
@RestController
@RequestMapping(ControladorImagen.RUTA)
public class ControladorImagen extends BaseController<ImagenRequestDTO, ImagenResponseDTO> {

    static final String RUTA = "/api/imagenes";

    private final ServicioImagen servicioImagen;

    public ControladorImagen(ServicioImagen servicioImagen) {
        super(servicioImagen);
        this.servicioImagen = servicioImagen;
    }

    @PostMapping(path = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponseDTO> subir(@RequestParam("archivo") MultipartFile archivo,
                                                   @RequestParam("tipoImagen") TipoImagen tipoImagen)
            throws MiException {
        ImagenResponseDTO creada = servicioImagen.alta(aDTO(archivo, tipoImagen));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(RUTA + "/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping(path = "/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenResponseDTO> reemplazarArchivo(@PathVariable String id,
                                                               @RequestParam("archivo") MultipartFile archivo,
                                                               @RequestParam("tipoImagen") TipoImagen tipoImagen)
            throws MiException {
        return ResponseEntity.ok(servicioImagen.modificar(id, aDTO(archivo, tipoImagen)));
    }

    @GetMapping("/{id}/contenido")
    public ResponseEntity<byte[]> contenido(@PathVariable String id) throws MiException {
        ImagenContenidoDTO imagen = servicioImagen.obtenerContenido(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.mime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(imagen.nombre(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(imagen.contenido());
    }

    /** Interpreta el archivo recibido por HTTP y lo convierte en el DTO que espera el servicio. */
    private static ImagenRequestDTO aDTO(MultipartFile archivo, TipoImagen tipoImagen) throws MiException {
        if (archivo == null || archivo.isEmpty()) {
            return new ImagenRequestDTO(null, null, null, tipoImagen);
        }
        try {
            return new ImagenRequestDTO(archivo.getOriginalFilename(), archivo.getContentType(), archivo.getBytes(),
                    tipoImagen);
        } catch (IOException e) {
            throw new MiException("No se pudo leer el archivo recibido");
        }
    }
}

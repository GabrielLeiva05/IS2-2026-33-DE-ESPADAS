package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Imagen;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.ImagenService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.util.Optional;

/** Entrega los bytes de una imagen para <img src="/imagenes/{id}">. No es un CRUD, por eso no extiende BaseController. */
@Controller
@RequestMapping("/imagenes")
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> ver(@PathVariable String id) {
        try {
            Optional<Imagen> imagen = imagenService.obtener(id);
            if (imagen.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagen.get().getMime()))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(imagen.get().getContenido());
        } catch (ErrorServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.example.securityDemo.controladores;

import com.example.securityDemo.entidades.Usuario;
import com.example.securityDemo.servicio.UsuarioServicio;
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

    private final UsuarioServicio usuarioServicio;

    // Inyección por constructor (estándar recomendado en Spring Boot moderno)
    public ImagenControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/perfil/{id}")
    public ResponseEntity<byte[]> imagenUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioServicio.getById(id);

        if (usuario == null || usuario.getImagen() == null || usuario.getImagen().getContenido() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] imagen = usuario.getImagen().getContenido();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imagen);
    }
}
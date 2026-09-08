package com.is2.tinder.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.is2.tinder.dtos.FotoDTO;
import com.is2.tinder.services.MascotaService;
import com.is2.tinder.services.UsuarioService;

@Controller
public class FotoController {
    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;

    public FotoController(UsuarioService usuarioService, MascotaService mascotaService) {
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
    }

    @GetMapping("/foto/usuario/{id}")
    public ResponseEntity<byte[]> usuario(@PathVariable String id) {
        try { var user = usuarioService.buscarUsuario(id); return foto(user == null ? null : user.getFoto()); }
        catch (Exception exception) { return ResponseEntity.notFound().build(); }
    }

    @GetMapping("/foto/mascota/{id}")
    public ResponseEntity<byte[]> mascota(@PathVariable String id) {
        try { var pet = mascotaService.buscarMascota(id); return foto(pet == null ? null : pet.getFoto()); }
        catch (Exception exception) { return ResponseEntity.notFound().build(); }
    }

    private ResponseEntity<byte[]> foto(FotoDTO foto) {
        if (foto == null || foto.getContenido() == null) return ResponseEntity.notFound().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(foto.getMime() == null ? "application/octet-stream" : foto.getMime()));
        return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);
    }
}
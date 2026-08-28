package com.is2.tinder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.is2.tinder.dtos.UsuarioDTO;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.services.VotoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class VotoController {
    private final VotoService votoService;

    public VotoController(VotoService votoService) { this.votoService = votoService; }

    @PostMapping("/votar")
    public String votar(HttpSession session, @RequestParam String idMascotaPropia,
            @RequestParam String idMascotaVotada, RedirectAttributes attributes) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuariosession");
        if (usuario == null) return "redirect:/login";
        try { votoService.votar(usuario.getId(), idMascotaPropia, idMascotaVotada); attributes.addFlashAttribute("success", "Voto enviado con éxito"); }
        catch (ErrorService exception) { attributes.addFlashAttribute("error", exception.getMessage()); }
        return "redirect:/mascota/explorar-mascotas?idMascotaPropia=" + idMascotaPropia;
    }

    @GetMapping("/votos/descargar")
    public ResponseEntity<String> descargarReporte(HttpSession session) {
        if (session.getAttribute("usuariosession") == null) return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/login").build();
        StringBuilder contenido = new StringBuilder("Nombre de Usuario;Apellido del Usuario;Nombre de la mascota;Cantidad de votos\n");
        votoService.listarReporteVotos().forEach(reporte -> contenido.append(reporte.getNombreUsuario()).append(';')
                .append(reporte.getApellidoUsuario()).append(';').append(reporte.getNombreMascota()).append(';')
                .append(reporte.getCantidadVotos()).append('\n'));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-votos.txt")
                .contentType(MediaType.TEXT_PLAIN).body(contenido.toString());
    }
}
package com.incisog.Clinica.controller.vista;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.FotoPacienteService;
import com.incisog.Clinica.models.FotoPaciente;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * FRONTEND JAVA — foto del paciente (equivalente a FotoPacienteCard.jsx). Mismo molde que el
 * FotoController del tinder:
 *   - GET  devuelve los BYTES de la imagen (no una vista): lo usa el <img src="..."> de historia.html.
 *     Es un @Controller, pero este método devuelve ResponseEntity<byte[]>, así que Spring
 *     manda los bytes tal cual en vez de buscar un template.
 *   - POST recibe el <form enctype="multipart/form-data"> y vuelve a la ficha.
 */
@Controller
@RequestMapping("/pacientes/{pacienteId}/foto")
public class FotoVistaController {

    private final FotoPacienteService service;

    public FotoVistaController(FotoPacienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<byte[]> ver(@PathVariable String pacienteId) {
        try {
            FotoPaciente foto = service.buscarPorPaciente(pacienteId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(foto.getMime()))
                    .body(foto.getContenido());
        } catch (ErrorServiceException exception) {
            return ResponseEntity.notFound().build(); // sin foto → 404 (el <img> no muestra nada)
        }
    }

    @PostMapping
    public String subir(HttpSession session, RedirectAttributes redirect, @PathVariable String pacienteId,
                        @RequestParam(required = false) MultipartFile archivo) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            service.subir(pacienteId, archivo);
            redirect.addFlashAttribute("exito", "Foto actualizada.");
        } catch (ErrorServiceException exception) {
            redirect.addFlashAttribute("error", exception.getMessage()); // "El archivo debe ser una imagen"
        }
        return "redirect:/pacientes/{pacienteId}/historia";
    }
}

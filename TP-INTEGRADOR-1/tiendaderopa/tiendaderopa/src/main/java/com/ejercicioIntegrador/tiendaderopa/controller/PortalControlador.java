package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.service.UsuarioServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Controller
public class PortalControlador {

    private final UsuarioServicio usuarioServicio;

    public PortalControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_JEFE')")
    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/registrar")
    public String registrar() {
        return "register.html";
    }

    @PostMapping("/registro")
    public String registro(
            @RequestParam(required = false) MultipartFile archivo,
            @RequestParam String documento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String clave,
            @RequestParam String clave2,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaNacimiento,
            ModelMap modelo) {
        try {
            usuarioServicio.registrar(documento, tipoDocumento, nombre, apellido,
                    email, clave, clave2, fechaNacimiento);
            modelo.put("exito", "Usuario registrado correctamente");
            return "index.html";
        } catch (MiException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("documento", documento);
            modelo.put("tipoDocumento", tipoDocumento);
            modelo.put("nombre", nombre);
            modelo.put("apellido", apellido);
            modelo.put("email", email);
            modelo.put("fechaNacimiento", fechaNacimiento);
            return "register.html";
        } catch (Exception ex) {
            modelo.put("error", "Error inesperado del sistema: " + ex.getMessage());
            return "register.html";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "Usuario o contraseña inválidos");
        }

        return "login.html";
    }
}

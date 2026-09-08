package com.is2.tinder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.services.UsuarioService;
import com.is2.tinder.services.ZonaService;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class PortalController {
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;

    public PortalController(UsuarioService usuarioService, ZonaService zonaService) {
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
    }

    /*@GetMapping({"/", "/index"})
    public String index() { return "index.html"; }*/

    @GetMapping({"/", "/index", "/inicio"})
    public String inicio(HttpSession session, HttpServletRequest request) {
        if (session.getAttribute("usuariosession") == null) {
            Cookie cookie = buscarCookie(request, "usuarioRecordado");
            if (cookie != null) {
                try { session.setAttribute("usuariosession", usuarioService.buscarUsuario(cookie.getValue())); }
                catch (ErrorService ignored) { }
            }
        }
        return session.getAttribute("usuariosession") == null ? "redirect:/login" : "inicio.html";
    }

    @GetMapping("/login")
    public String login(HttpSession session, HttpServletRequest request) {
        if (session.getAttribute("usuariosession") == null) {
            Cookie cookie = buscarCookie(request, "usuarioRecordado");
            if (cookie != null) {
                try {
                    session.setAttribute("usuariosession", usuarioService.buscarUsuario(cookie.getValue()));
                    if (session.getAttribute("usuariosession") != null) return "redirect:/inicio";
                } catch (ErrorService ignored) { }
            }
        }
        return "login.html";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("usuarioRecordado", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    private Cookie buscarCookie(HttpServletRequest request, String nombre) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) if (nombre.equals(cookie.getName())) return cookie;
        return null;
    }

    @GetMapping("/registro")
    public String registro(ModelMap model) {
        try {
            model.put("zonas", zonaService.listarZona());
        } catch (ErrorService exception) {
            model.put("error", exception.getMessage());
        }
        return "registro.html";
    }

    @PostMapping("/registrar")
    public String registrar(ModelMap model, @RequestParam String nombre, @RequestParam String apellido,
            @RequestParam String mail, @RequestParam String clave1, @RequestParam String clave2,
            @RequestParam String idZona, MultipartFile archivo) {
        try {
            usuarioService.registrar(nombre, apellido, mail, clave1, clave2, archivo, idZona);
            model.put("titulo", "Bienvenido al Tinder de Mascotas!");
            model.put("descripcion", "Tu usuario fue registrado de manera exitosa");
            return "exito.html";
        } catch (ErrorService exception) {
            model.put("error", exception.getMessage());
            model.put("nombre", nombre);
            model.put("apellido", apellido);
            model.put("mail", mail);
            try { model.put("zonas", zonaService.listarZona()); } catch (ErrorService ignored) { }
            return "registro.html";
        }
    }
}
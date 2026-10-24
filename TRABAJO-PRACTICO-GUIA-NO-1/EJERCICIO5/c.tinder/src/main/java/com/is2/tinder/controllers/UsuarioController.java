package com.is2.tinder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.dtos.UsuarioDTO;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.services.UsuarioService;
import com.is2.tinder.services.ZonaService;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {
    private static final String COOKIE_USUARIO = "usuarioRecordado";
    private static final int COOKIE_DURACION_SEGUNDOS = 2 * 24 * 60 * 60;
    private final UsuarioService usuarioService;
    private final ZonaService zonaService;

    public UsuarioController(UsuarioService usuarioService, ZonaService zonaService) {
        this.usuarioService = usuarioService;
        this.zonaService = zonaService;
    }

    @PostMapping("/loginUsuario")
    public String login(@RequestParam String email, @RequestParam String clave, ModelMap model, HttpSession session,
            HttpServletResponse response) {
        try {
            UsuarioDTO usuario = usuarioService.login(email, clave);
            session.setAttribute("usuariosession", usuario);
            agregarCookie(response, usuario.getId());
            return "redirect:/inicio";
        } catch (ErrorService exception) {
            model.put("error", exception.getMessage());
            model.put("email", email == null ? null : email.trim());
            return "login.html";
        }
    }

    @GetMapping("/editar-perfil")
    public String editarPerfil(HttpSession session, ModelMap model) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuariosession");
        if (usuario == null) return "redirect:/login";
        model.put("perfil", usuario);
        try { model.put("zonas", zonaService.listarZona()); } catch (ErrorService exception) { model.put("error", exception.getMessage()); }
        return "perfil.html";
    }

    @PostMapping("/actualizar-perfil")
    public String actualizarPerfil(HttpSession session, ModelMap model, @RequestParam String id,
            @RequestParam String nombre, @RequestParam String apellido, @RequestParam String mail,
            @RequestParam String clave1, @RequestParam String clave2, @RequestParam String idZona,
            MultipartFile archivo) {
        UsuarioDTO login = (UsuarioDTO) session.getAttribute("usuariosession");
        if (login == null || !login.getId().equals(id)) return "redirect:/login";
        try {
            usuarioService.modificar(id, nombre, apellido, mail, clave1, clave2, archivo, idZona);
            session.setAttribute("usuariosession", usuarioService.buscarUsuario(id));
            return "redirect:/inicio";
        } catch (ErrorService exception) {
            model.put("perfil", login);
            model.put("error", exception.getMessage());
            try { model.put("zonas", zonaService.listarZona()); } catch (ErrorService ignored) { }
            return "perfil.html";
        }
    }

    private void agregarCookie(HttpServletResponse response, String idUsuario) {
        Cookie cookie = new Cookie(COOKIE_USUARIO, idUsuario);
        cookie.setMaxAge(COOKIE_DURACION_SEGUNDOS);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue()
                + "; Max-Age=" + cookie.getMaxAge() + "; Path=/; HttpOnly; SameSite=Lax");
    }
}
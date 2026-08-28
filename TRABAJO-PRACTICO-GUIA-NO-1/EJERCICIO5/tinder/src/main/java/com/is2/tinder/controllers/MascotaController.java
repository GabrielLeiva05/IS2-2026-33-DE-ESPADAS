package com.is2.tinder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.dtos.UsuarioDTO;
import com.is2.tinder.dtos.MascotaDTO;
import com.is2.tinder.enumerations.Sexo;
import com.is2.tinder.enumerations.Tipo;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.services.MascotaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mascota")
public class MascotaController {
    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) { this.mascotaService = mascotaService; }

    @GetMapping("/mis-mascotas")
    public String misMascotas(HttpSession session, ModelMap model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user == null) return "redirect:/login";
        try { model.put("mascotas", mascotaService.listarMascotaPorUsuario(user.getId())); return "mascotas.html"; }
        catch (ErrorService exception) { model.put("error", exception.getMessage()); return "error.html"; }
    }

    @GetMapping("/debaja-mascotas")
    public String dadasDeBaja(HttpSession session, ModelMap model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user == null) return "redirect:/login";
        try { model.put("mascotas", mascotaService.listarMascotaDeBaja(user.getId())); return "mascotasdebaja.html"; }
        catch (ErrorService exception) { model.put("error", exception.getMessage()); return "error.html"; }
    }

    @GetMapping("/editar-perfil")
    public String editar(HttpSession session, @RequestParam(required = false) String id,
            @RequestParam(defaultValue = "Crear") String accion, ModelMap model) {
        if (session.getAttribute("usuariosession") == null) return "redirect:/login";
        try { model.put("perfil", id == null ? new MascotaDTO() : mascotaService.buscarMascota(id)); }
        catch (ErrorService exception) { model.put("error", exception.getMessage()); model.put("perfil", new MascotaDTO()); }
        model.put("accion", accion); model.put("sexos", Sexo.values()); model.put("tipos", Tipo.values());
        return "mascota.html";
    }

    @PostMapping("/actualizar-perfil")
    public String actualizar(HttpSession session, ModelMap model, @RequestParam String id, @RequestParam String nombre,
            @RequestParam Sexo sexo, @RequestParam Tipo tipo, MultipartFile archivo) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user == null) return "redirect:/login";
        try {
            if (id == null || id.isBlank()) mascotaService.agregarMascota(user.getId(), nombre, sexo, archivo, tipo);
            else mascotaService.modificar(user.getId(), id, nombre, sexo, archivo, tipo);
            return "redirect:/mascota/mis-mascotas";
        } catch (ErrorService exception) { model.put("error", exception.getMessage()); return "mascota.html"; }
    }

    @PostMapping("/eliminar-perfil")
    public String eliminar(HttpSession session, @RequestParam String id) throws ErrorService {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user != null) mascotaService.eliminar(user.getId(), id);
        return "redirect:/mascota/mis-mascotas";
    }

    @PostMapping("/alta-perfil")
    public String alta(HttpSession session, @RequestParam String id) throws ErrorService {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user != null) mascotaService.darAlta(user.getId(), id);
        return "redirect:/mascota/mis-mascotas";
    }

    @GetMapping("/explorar-mascotas")
    public String explorar(HttpSession session, @RequestParam(required = false) String idMascotaPropia, ModelMap model) throws ErrorService {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("usuariosession");
        if (user == null) return "redirect:/login";
        model.put("misMascotas", mascotaService.listarMascotaPorUsuario(user.getId()));
        if (idMascotaPropia != null && !idMascotaPropia.isBlank()) {
            MascotaDTO selected = mascotaService.buscarMascota(idMascotaPropia);
            model.put("mascotaSeleccionada", selected);
            model.put("mascotas", mascotaService.listarMascotasPorTipo(user.getId(), selected == null ? null : selected.getTipo()));
        }
        return "mascotas-explorar.html";
    }
}
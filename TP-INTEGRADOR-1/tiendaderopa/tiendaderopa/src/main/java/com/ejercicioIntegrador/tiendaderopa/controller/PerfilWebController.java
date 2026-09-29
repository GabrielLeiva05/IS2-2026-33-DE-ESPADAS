package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.dto.ActualizarPerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.PerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import com.ejercicioIntegrador.tiendaderopa.service.PerfilServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilWebController {

    private final PerfilServicio perfilServicio;
    private final LocalidadServicio localidadServicio;

    public PerfilWebController(PerfilServicio perfilServicio, LocalidadServicio localidadServicio) {
        this.perfilServicio = perfilServicio;
        this.localidadServicio = localidadServicio;
    }

    @GetMapping("/perfil")
    public String ver(@AuthenticationPrincipal UserDetails usuario, Model model) throws MiException {
        PerfilDTO perfil = perfilServicio.obtener(usuario.getUsername());
        model.addAttribute("perfil", perfil);
        model.addAttribute("datos", aFormulario(perfil));
        cargarLocalidades(model);
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardar(@AuthenticationPrincipal UserDetails usuario,
            @Valid @ModelAttribute("datos") ActualizarPerfilDTO datos,
            BindingResult resultado, Model model, RedirectAttributes redirect) throws MiException {
        if (resultado.hasErrors()) {
            model.addAttribute("perfil", perfilServicio.obtener(usuario.getUsername()));
            cargarLocalidades(model);
            return "perfil";
        }

        perfilServicio.actualizar(usuario.getUsername(), datos);
        redirect.addFlashAttribute("exito", "Los datos del perfil se actualizaron correctamente");
        return "redirect:/perfil";
    }

    private void cargarLocalidades(Model model) {
        model.addAttribute("localidades", localidadServicio.listarTodas());
    }

    private ActualizarPerfilDTO aFormulario(PerfilDTO perfil) {
        return new ActualizarPerfilDTO(perfil.nombre(), perfil.apellido(), perfil.sexo(),
                perfil.fechaNacimiento(), perfil.telefono(), perfil.codigoPostal(), perfil.barrio(),
                perfil.calle(), perfil.numeracion(), perfil.manzanaPiso(), perfil.casaDepartamento(),
                perfil.referencia(), perfil.localidadId());
    }
}
package com.incisog.Clinica.controller.vista;

import com.incisog.Clinica.logic.dto.PacienteDTO;
import com.incisog.Clinica.logic.dto.adapter.PacienteAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.PacienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * FRONTEND JAVA — ABM de pacientes (equivalente a PacientesPage.jsx).
 * Mismo molde que MedicoVistaController. La foto y la historia tienen sus propios controllers.
 */
@Controller
@RequestMapping("/pacientes")
public class PacienteVistaController {

    private final PacienteService service;
    private final PacienteAdapter adapter;

    public PacienteVistaController(PacienteService service, PacienteAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public String listar(HttpSession session, ModelMap model, @RequestParam(required = false) String editar) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";

        PacienteDTO form = new PacienteDTO();
        if (editar != null) {
            try {
                form = adapter.toDTO(service.buscarPorId(editar));
            } catch (ErrorServiceException exception) {
                model.put("error", exception.getMessage());
            }
        }
        return mostrar(model, form);
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, ModelMap model, RedirectAttributes redirect,
                          @ModelAttribute("form") PacienteDTO form) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            if (form.getId() == null || form.getId().isBlank()) {
                service.crear(adapter.toEntity(form));
                redirect.addFlashAttribute("exito", "Paciente creado.");
            } else {
                service.modificar(form.getId(), adapter.toEntity(form));
                redirect.addFlashAttribute("exito", "Paciente modificado.");
            }
            return "redirect:/pacientes";
        } catch (ErrorServiceException exception) {
            model.put("error", exception.getMessage());
            return mostrar(model, form);
        }
    }

    @PostMapping("/eliminar")
    public String eliminar(HttpSession session, RedirectAttributes redirect, @RequestParam String id) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Paciente eliminado.");
        } catch (ErrorServiceException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/pacientes";
    }

    private String mostrar(ModelMap model, PacienteDTO form) {
        model.put("pacientes", adapter.toDTOList(service.listar()));
        model.put("form", form);
        model.put("activo", "pacientes");
        return "pacientes";
    }
}

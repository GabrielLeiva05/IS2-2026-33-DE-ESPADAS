package com.incisog.Clinica.controller.vista;

import com.incisog.Clinica.logic.dto.MedicoDTO;
import com.incisog.Clinica.logic.dto.adapter.MedicoAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.MedicoService;
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
 * FRONTEND JAVA — ABM de médicos (equivalente a MedicosPage.jsx).
 *
 * Usa EXACTAMENTE lo mismo que MedicoController (REST): MedicoService + MedicoAdapter
 * (y por dentro del adapter, MedicoCreator → Factory Method). Solo cambia el "borde":
 *   REST:  JSON  --Jackson-->     MedicoDTO  → adapter → service → adapter → JSON
 *   Vista: form  --@ModelAttribute--> MedicoDTO → adapter → service → adapter → HTML
 *
 * Los formularios HTML solo saben hacer GET y POST (no PUT ni DELETE), por eso
 * modificar y eliminar son POST a /guardar y /eliminar.
 */
@Controller
@RequestMapping("/medicos")
public class MedicoVistaController {

    private final MedicoService service;
    private final MedicoAdapter adapter;

    public MedicoVistaController(MedicoService service, MedicoAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    /** GET /medicos            → lista + formulario vacío (alta)
     *  GET /medicos?editar={id} → lista + formulario cargado con ese médico (modificación) */
    @GetMapping
    public String listar(HttpSession session, ModelMap model, @RequestParam(required = false) String editar) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";

        MedicoDTO form = new MedicoDTO();
        if (editar != null) {
            try {
                form = adapter.toDTO(service.buscarPorId(editar));
            } catch (ErrorServiceException exception) {
                model.put("error", exception.getMessage());
            }
        }
        return mostrar(model, form);
    }

    /**
     * @ModelAttribute("form"): Spring crea un MedicoDTO y le llena los atributos con los campos
     * del formulario que tengan el mismo "name" (id, nombre, apellido, documento).
     * Es el equivalente de @RequestBody, pero para formularios HTML en vez de JSON.
     */
    @PostMapping("/guardar")
    public String guardar(HttpSession session, ModelMap model, RedirectAttributes redirect,
                          @ModelAttribute("form") MedicoDTO form) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            if (form.getId() == null || form.getId().isBlank()) {
                service.crear(adapter.toEntity(form));
                redirect.addFlashAttribute("exito", "Médico creado.");
            } else {
                service.modificar(form.getId(), adapter.toEntity(form));
                redirect.addFlashAttribute("exito", "Médico modificado.");
            }
            // Patrón POST-Redirect-GET: si el usuario aprieta F5 no se vuelve a enviar el formulario.
            return "redirect:/medicos";
        } catch (ErrorServiceException exception) {
            // Error: NO se redirige; se vuelve a mostrar la pantalla con lo que escribió + el mensaje.
            model.put("error", exception.getMessage());
            return mostrar(model, form);
        }
    }

    @PostMapping("/eliminar")
    public String eliminar(HttpSession session, RedirectAttributes redirect, @RequestParam String id) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            service.eliminar(id);
            redirect.addFlashAttribute("exito", "Médico eliminado.");
        } catch (ErrorServiceException exception) {
            // flash: el mensaje sobrevive al redirect (se guarda en sesión hasta el próximo GET)
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/medicos";
    }

    /** Lo que necesita medicos.html para dibujarse. */
    private String mostrar(ModelMap model, MedicoDTO form) {
        model.put("medicos", adapter.toDTOList(service.listar()));
        model.put("form", form);
        model.put("activo", "medicos"); // para resaltar el link del menú lateral
        return "medicos";
    }
}

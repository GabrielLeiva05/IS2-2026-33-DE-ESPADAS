package com.incisog.Clinica.controller.vista;

import com.incisog.Clinica.logic.dto.HistoriaClinicaDTO;
import com.incisog.Clinica.logic.dto.UsuarioDTO;
import com.incisog.Clinica.logic.dto.adapter.HistoriaClinicaAdapter;
import com.incisog.Clinica.logic.dto.adapter.MedicoAdapter;
import com.incisog.Clinica.logic.dto.adapter.PacienteAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.DetalleHistoriaClinicaService;
import com.incisog.Clinica.logic.service.HistoriaClinicaService;
import com.incisog.Clinica.logic.service.MedicoService;
import com.incisog.Clinica.logic.service.PacienteService;
import com.incisog.Clinica.models.DetalleHistoriaClinica;
import com.incisog.Clinica.models.HistoriaClinica;
import com.incisog.Clinica.models.Medico;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * FRONTEND JAVA — ficha del paciente con su historia clínica (equivalente a HistoriaPacientePage.jsx
 * + DetalleForm + DetallesLista + FiltroDetalles).
 *
 * En React, la página hacía 3-4 fetch y armaba la pantalla en el navegador.
 * Acá, UN solo GET junta todo en el ModelMap (paciente, médicos, historia, detalles, filtro)
 * y Thymeleaf devuelve la pantalla completa.
 *
 * Todas las URLs cuelgan del paciente: /pacientes/{pacienteId}/historia/...
 */
@Controller
@RequestMapping("/pacientes/{pacienteId}/historia")
public class HistoriaVistaController {

    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final HistoriaClinicaService historiaService;
    private final DetalleHistoriaClinicaService detalleService;
    private final PacienteAdapter pacienteAdapter;
    private final MedicoAdapter medicoAdapter;
    private final HistoriaClinicaAdapter historiaAdapter;

    public HistoriaVistaController(PacienteService pacienteService, MedicoService medicoService,
                                   HistoriaClinicaService historiaService, DetalleHistoriaClinicaService detalleService,
                                   PacienteAdapter pacienteAdapter, MedicoAdapter medicoAdapter,
                                   HistoriaClinicaAdapter historiaAdapter) {
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.historiaService = historiaService;
        this.detalleService = detalleService;
        this.pacienteAdapter = pacienteAdapter;
        this.medicoAdapter = medicoAdapter;
        this.historiaAdapter = historiaAdapter;
    }

    /**
     * GET /pacientes/{id}/historia
     *   ?criterio=medico&valor=perez → con filtro (Strategy en DetalleHistoriaClinicaService.buscar)
     *   ?editar={detalleId}          → el formulario se carga con esa atención
     */
    @GetMapping
    public String ver(HttpSession session, ModelMap model, @PathVariable String pacienteId,
                      @RequestParam(required = false) String criterio,
                      @RequestParam(required = false) String valor,
                      @RequestParam(required = false) String editar) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        model.put("activo", "pacientes");

        // 1) El paciente. Si no existe, la vista muestra solo el error y "Volver".
        try {
            model.put("paciente", pacienteAdapter.toDTO(pacienteService.buscarPorId(pacienteId)));
        } catch (ErrorServiceException exception) {
            model.put("error", exception.getMessage());
            return "historia";
        }
        model.put("medicos", medicoAdapter.toDTOList(medicoService.listar()));
        // Cambia en cada carga: la URL de la foto queda distinta y el navegador no muestra la vieja (caché).
        model.put("version", System.currentTimeMillis());

        // 2) La historia. Que no tenga NO es un error: la vista muestra el botón "Crear historia clínica".
        HistoriaClinica historia = buscarHistoria(pacienteId);
        if (historia == null) return "historia";
        model.put("historia", historiaAdapter.toDTO(historia));

        // 3) Las atenciones: todas, o filtradas si vino un criterio.
        List<DetalleHistoriaClinica> detalles;
        try {
            if (criterio != null && !criterio.isBlank()) {
                detalles = detalleService.buscar(historia.getId(), criterio, valor); // Strategy
                model.put("filtroCriterio", criterio);
                model.put("filtroValor", valor);
            } else {
                detalles = detalleService.listar(historia.getId());
            }
        } catch (ErrorServiceException exception) {
            // Ej: "Debe indicar un valor de búsqueda" → se avisa y se muestran todas (como en React).
            model.put("error", exception.getMessage());
            detalles = listarSinFiltro(historia.getId());
        }
        model.put("detalles", detalles);

        // 4) El formulario. Si vino de un POST con error, ya está en el modelo (flash attribute):
        //    se respeta para no perder lo que el usuario escribió.
        if (!model.containsAttribute("form")) {
            model.put("form", formularioInicial(detalles, editar));
        }
        return "historia";
    }

    /** POST /pacientes/{id}/historia/crear — botón "Crear historia clínica". */
    @PostMapping("/crear")
    public String crear(HttpSession session, RedirectAttributes redirect, @PathVariable String pacienteId) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(PortalController.USUARIO_SESION);
        if (usuario == null) return "redirect:/login";
        try {
            // Mismo DTO que manda React: {pacienteId, usuarioId}. El usuario sale de la sesión.
            HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
            dto.setPacienteId(pacienteId);
            dto.setUsuarioId(usuario.getId());
            historiaService.crear(historiaAdapter.toEntity(dto));
            redirect.addFlashAttribute("exito", "Historia clínica creada.");
        } catch (ErrorServiceException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/pacientes/{pacienteId}/historia";
    }

    /** POST .../detalles/guardar — alta o modificación de una atención (según venga o no el id). */
    @PostMapping("/detalles/guardar")
    public String guardarDetalle(HttpSession session, RedirectAttributes redirect, @PathVariable String pacienteId,
                                 @ModelAttribute DetalleFormulario form,
                                 @RequestParam(required = false) String criterio,
                                 @RequestParam(required = false) String valor) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            HistoriaClinica historia = historiaService.buscarPorPaciente(pacienteId);
            DetalleHistoriaClinica detalle = armarDetalle(form);
            if (form.isEditando()) {
                detalleService.modificar(historia.getId(), form.getId(), detalle);
                redirect.addFlashAttribute("exito", "Atención modificada.");
            } else {
                detalleService.agregar(historia.getId(), detalle);
                redirect.addFlashAttribute("exito", "Atención agregada.");
            }
        } catch (ErrorServiceException exception) {
            // Ej: "La fecha no puede ser futura". Se devuelve el formulario tal cual lo dejó el usuario.
            redirect.addFlashAttribute("error", exception.getMessage());
            redirect.addFlashAttribute("form", form);
        }
        mantenerFiltro(redirect, criterio, valor);
        return "redirect:/pacientes/{pacienteId}/historia";
    }

    /** POST .../detalles/eliminar — baja lógica de una atención. */
    @PostMapping("/detalles/eliminar")
    public String eliminarDetalle(HttpSession session, RedirectAttributes redirect, @PathVariable String pacienteId,
                                  @RequestParam String detalleId,
                                  @RequestParam(required = false) String criterio,
                                  @RequestParam(required = false) String valor) {
        if (session.getAttribute(PortalController.USUARIO_SESION) == null) return "redirect:/login";
        try {
            HistoriaClinica historia = historiaService.buscarPorPaciente(pacienteId);
            detalleService.eliminar(historia.getId(), detalleId);
            redirect.addFlashAttribute("exito", "Atención eliminada.");
        } catch (ErrorServiceException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        mantenerFiltro(redirect, criterio, valor);
        return "redirect:/pacientes/{pacienteId}/historia";
    }

    // ---------- auxiliares ----------

    private HistoriaClinica buscarHistoria(String pacienteId) {
        try {
            return historiaService.buscarPorPaciente(pacienteId);
        } catch (ErrorServiceException exception) {
            return null; // "El paciente no tiene historia clínica."
        }
    }

    private List<DetalleHistoriaClinica> listarSinFiltro(String historiaId) {
        try {
            return detalleService.listar(historiaId);
        } catch (ErrorServiceException exception) {
            return List.of();
        }
    }

    /** Formulario vacío (fecha de hoy) o cargado con la atención que se quiere editar. */
    private DetalleFormulario formularioInicial(List<DetalleHistoriaClinica> detalles, String editar) {
        if (editar != null) {
            for (DetalleHistoriaClinica d : detalles) {
                if (d.getId().equals(editar)) {
                    return new DetalleFormulario(d.getId(), d.getFechaHistoria().toString(),
                            d.getMedico() != null ? d.getMedico().getId() : "", d.getDetalleHistoria());
                }
            }
        }
        // LocalDate.now() usa la zona del SERVIDOR: el mismo criterio con el que valida el service.
        return new DetalleFormulario("", LocalDate.now().toString(), "", "");
    }

    /**
     * Formulario → entidad, con la misma forma que mandaba React: {"medico": {"id": "..."}}.
     * Así el service (validar, buscarMedico) no cambia en nada.
     */
    private DetalleHistoriaClinica armarDetalle(DetalleFormulario form) throws ErrorServiceException {
        DetalleHistoriaClinica detalle = new DetalleHistoriaClinica();
        detalle.setDetalleHistoria(form.getDetalleHistoria());
        if (form.getFechaHistoria() != null && !form.getFechaHistoria().isBlank()) {
            try {
                detalle.setFechaHistoria(LocalDate.parse(form.getFechaHistoria()));
            } catch (DateTimeParseException e) {
                throw new ErrorServiceException("La fecha no es válida");
            }
        } // si viene vacía queda null → el service responde "Debe indicar la fecha"
        if (form.getMedicoId() != null && !form.getMedicoId().isBlank()) {
            Medico medico = new Medico();
            medico.setId(form.getMedicoId());
            detalle.setMedico(medico);
        } // si no eligió médico queda null → "Debe indicar el médico"
        return detalle;
    }

    /** Después de guardar/eliminar se vuelve con el mismo filtro que había (como hacía React). */
    private void mantenerFiltro(RedirectAttributes redirect, String criterio, String valor) {
        if (criterio != null && !criterio.isBlank()) {
            redirect.addAttribute("criterio", criterio); // addAttribute (no flash) → va como ?criterio=...
            redirect.addAttribute("valor", valor);
        }
    }
}

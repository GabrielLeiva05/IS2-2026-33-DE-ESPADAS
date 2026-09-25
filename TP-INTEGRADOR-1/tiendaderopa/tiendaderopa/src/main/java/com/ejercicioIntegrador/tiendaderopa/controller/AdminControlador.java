package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class AdminControlador {

    private final PaisServicio paisServicio;
    private final ProvinciaServicio provinciaServicio;
    private final DepartamentoServicio departamentoServicio;
    private final LocalidadServicio localidadServicio;
    private final DireccionServicio direccionServicio;
    public AdminControlador(PaisServicio paisServicio,
                            ProvinciaServicio provinciaServicio,
                            DepartamentoServicio departamentoServicio,
                            LocalidadServicio localidadServicio,
                            DireccionServicio direccionServicio) {
        this.paisServicio = paisServicio;
        this.provinciaServicio = provinciaServicio;
        this.departamentoServicio = departamentoServicio;
        this.localidadServicio = localidadServicio;
        this.direccionServicio = direccionServicio;
    }
    @GetMapping("/dashboard")
    public String dashboard(ModelMap modelo) {
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("pais", null);
        modelo.addAttribute("provincias", provinciaServicio.listarTodas());
        modelo.addAttribute("provincia", null);
        modelo.addAttribute("departamentos", departamentoServicio.listarTodos());
        modelo.addAttribute("departamento", null);
        modelo.addAttribute("localidades", localidadServicio.listarTodas());
        modelo.addAttribute("localidad", null);
        modelo.addAttribute("direcciones", direccionServicio.listarTodas());
        modelo.addAttribute("direccion", null);
        return "panel.html";
    }
}
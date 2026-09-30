package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoSucursal;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioEmpresa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/empresas")
public class EmpresaControlador {

    private final ServicioEmpresa servicio;

    public EmpresaControlador(ServicioEmpresa servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarEmpresa(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/empresas";
        }
    }

    @PostMapping
    public String crear(
            @RequestParam String razonSocial,
            @RequestParam String cuit,
            @RequestParam TipoSucursal tipoSucursal,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearEmpresa(razonSocial, cuit, tipoSucursal);
            redirect.addFlashAttribute("mensaje", "Empresa creada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empresas";
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String razonSocial,
            @RequestParam String cuit,
            @RequestParam TipoSucursal tipoSucursal,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarEmpresa(id, razonSocial, cuit, tipoSucursal);
            redirect.addFlashAttribute("mensaje", "Empresa actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empresas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarEmpresa(id);
            redirect.addFlashAttribute("mensaje", "Empresa eliminada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empresas";
    }

    private String mostrar(Model model, Empresa seleccionada) {
        AdminPageSupport.cargar(model, "Empresas", "/admin/empresas", Empresa.class,
                servicio.listarEmpresa(), java.util.List.of(
                        AdminPageSupport.campo("razonSocial", "text", true),
                        AdminPageSupport.campo("cuit", "text", true),
                        AdminPageSupport.campo("tipoSucursal", "select", true,
                                java.util.Arrays.stream(TipoSucursal.values()).map(Enum::name).toArray(String[]::new))),
                seleccionada);
        return "admin/registros";
    }
}
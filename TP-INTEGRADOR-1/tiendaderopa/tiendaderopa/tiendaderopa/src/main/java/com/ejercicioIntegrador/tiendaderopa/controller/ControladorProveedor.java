package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioRegistroProveedor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
@RequestMapping("/admin/proveedores")
public class ControladorProveedor {

    private final ServicioProveedor svcProveedor;
    private final ServicioRegistroProveedor svcRegistroProveedor;

    public ControladorProveedor(ServicioProveedor svcProveedor, ServicioRegistroProveedor svcRegistroProveedor) {
        this.svcProveedor = svcProveedor;
        this.svcRegistroProveedor = svcRegistroProveedor;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/activos")
    public String listarActivos(Model model) {
        AdminPageSupport.cargar(model, "Proveedores activos", "/admin/proveedores", Proveedor.class,
                svcProveedor.listarProveedorActivo(), java.util.List.of(
                        AdminPageSupport.campo("razonSocial", "text", true),
                        AdminPageSupport.campo("email", "email", false),
                        AdminPageSupport.campo("telefonoFijo", "tel", false),
                        AdminPageSupport.campo("telefonoCelular", "tel", false)), null);
        return "admin/registros";
    }

    @PostMapping
    public String crear(@RequestParam String razonSocial,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String telefonoFijo,
                                   @RequestParam(required = false) String telefonoCelular,
                                   RedirectAttributes redirect) {
        try {
            svcRegistroProveedor.registrarProveedor(razonSocial, email, telefonoFijo, telefonoCelular);
            redirect.addFlashAttribute("mensaje", "Proveedor creado correctamente.");
        } catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try { return mostrar(model, svcProveedor.buscarProveedor(id)); }
        catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/proveedores";
        }
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String razonSocial,
                            RedirectAttributes redirect) {
        try {
            svcProveedor.modificarProveedor(id, razonSocial);
            redirect.addFlashAttribute("mensaje", "Proveedor actualizado correctamente.");
        } catch (MiException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            svcProveedor.eliminarProveedor(id);
            redirect.addFlashAttribute("mensaje", "Proveedor eliminado correctamente.");
        } catch (MiException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/proveedores";
    }

    private String mostrar(Model model, Proveedor seleccionado) {
        AdminPageSupport.cargar(model, "Proveedores", "/admin/proveedores", Proveedor.class,
                svcProveedor.listarProveedor(), java.util.List.of(
                        AdminPageSupport.campo("razonSocial", "text", true),
                        AdminPageSupport.campo("email", "email", false),
                        AdminPageSupport.campo("telefonoFijo", "tel", false),
                        AdminPageSupport.campo("telefonoCelular", "tel", false)), seleccionado);
        return "admin/registros";
    }
}
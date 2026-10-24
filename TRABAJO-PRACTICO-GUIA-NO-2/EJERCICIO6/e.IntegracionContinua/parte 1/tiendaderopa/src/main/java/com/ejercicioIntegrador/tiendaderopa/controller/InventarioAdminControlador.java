package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioObjetivoReposicion;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioReporteProductos;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioSucursal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventario")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class InventarioAdminControlador {

    private final ServicioSucursal servicioSucursal;
    private final ServicioObjetivoReposicion servicioObjetivoReposicion;
    private final ServicioReporteProductos servicioReporteProductos;

        public InventarioAdminControlador(ServicioSucursal servicioSucursal,
            ServicioObjetivoReposicion servicioObjetivoReposicion,
            ServicioReporteProductos servicioReporteProductos) {
        this.servicioSucursal = servicioSucursal;
        this.servicioObjetivoReposicion = servicioObjetivoReposicion;
        this.servicioReporteProductos = servicioReporteProductos;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) String sucursalId, Model modelo) {
        Sucursal sucursal = sucursalId == null || sucursalId.isBlank()
                ? servicioSucursal.obtenerPrincipal()
                : servicioSucursal.buscarActiva(sucursalId);

        modelo.addAttribute("sucursales", servicioSucursal.listarActivas());
        modelo.addAttribute("sucursalSeleccionada", sucursal);
        modelo.addAttribute("reporte", servicioReporteProductos.generarReporte(sucursal.getId()));
        return "admin/inventario";
    }

    @PostMapping("/sucursales")
    public String crearSucursal(@RequestParam String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(defaultValue = "false") boolean principal,
            RedirectAttributes atributos) {
        String sucursalId = null;
        try {
            Sucursal sucursal = servicioSucursal.crear(nombre, direccion, principal);
            sucursalId = sucursal.getId();
            atributos.addFlashAttribute("exito", "Sucursal creada correctamente.");
        } catch (Exception ex) {
            atributos.addFlashAttribute("error", ex.getMessage());
        }
        return redirigir(atributos, sucursalId);
    }

    @PostMapping("/sucursales/principal")
    public String establecerPrincipal(@RequestParam String sucursalId, RedirectAttributes atributos) {
        try {
            servicioSucursal.establecerPrincipal(sucursalId);
            atributos.addFlashAttribute("exito", "Sucursal principal actualizada.");
        } catch (Exception ex) {
            atributos.addFlashAttribute("error", ex.getMessage());
        }
        return redirigir(atributos, sucursalId);
    }

    @PostMapping("/objetivos")
    public String configurarObjetivo(@RequestParam String sucursalId,
            @RequestParam String productoId,
            @RequestParam int cantidadObjetivo,
            RedirectAttributes atributos) {
        try {
            servicioObjetivoReposicion.configurar(sucursalId, productoId, cantidadObjetivo);
            atributos.addFlashAttribute("exito", "Objetivo de reposición actualizado.");
        } catch (Exception ex) {
            atributos.addFlashAttribute("error", ex.getMessage());
        }
        return redirigir(atributos, sucursalId);
    }

    private String redirigir(RedirectAttributes atributos, String sucursalId) {
        if (sucursalId != null && !sucursalId.isBlank()) {
            atributos.addAttribute("sucursalId", sucursalId);
        }
        return "redirect:/admin/inventario";
    }
}
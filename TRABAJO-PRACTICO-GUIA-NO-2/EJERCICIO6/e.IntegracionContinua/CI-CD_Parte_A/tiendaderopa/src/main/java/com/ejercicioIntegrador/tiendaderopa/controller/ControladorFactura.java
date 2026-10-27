package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFactura;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFacturaProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Pantalla "Facturas" del panel admin. Es la excepción que ya habíamos
 * marcado: acá SÍ hace falta el Controller de la base, porque es la
 * única vista que necesita mezclar Cliente y Proveedor juntos.
 *
 * Filtros soportados:
 *  - tipo: null (todas) | "CLIENTE" | "PROVEEDOR"
 *  - estado: null (todos los estados) | PAGADA | ANULADA | SIN_DEFINIR
 *  - idPersona: TODAVÍA NO IMPLEMENTADO (ver comentario abajo)
 */
@Controller
public class ControladorFactura {

    @Autowired
    private ServicioFactura svcFactura;
    @Autowired
    private ServicioFacturaCliente svcFacturaCliente;
    @Autowired
    private ServicioFacturaProveedor svcFacturaProveedor;

    @GetMapping("/facturas")
    public String listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) EstadoFactura estado,
            Model model) {
        try {
            Collection<? extends Factura> facturas;

            if ("CLIENTE".equalsIgnoreCase(tipo)) {
                facturas = (estado != null)
                        ? svcFacturaCliente.listarPorEstado(estado)
                        : svcFacturaCliente.listarActivo();
            } else if ("PROVEEDOR".equalsIgnoreCase(tipo)) {
                facturas = (estado != null)
                        ? svcFacturaProveedor.listarPorEstado(estado)
                        : svcFacturaProveedor.listarActivo();
            } else {
                // tipo == null → la vista mezclada, usando la base
                facturas = (estado != null)
                        ? svcFactura.listarFacturaPorEstado(estado)
                        : svcFactura.listarFacturaActivo();
            }
            // En la vista Thymeleaf: ${factura.tipoFactura} muestra "Cliente" o "Proveedor" sin lógica extra

            model.addAttribute("facturas", facturas);
            return "views/facturas/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/facturas/eliminar/{id}")
    public String eliminar(@PathVariable String id, Model model) {
        try {
            svcFactura.eliminarFactura(id); // el mismo método sirve para ambos tipos
            return "redirect:/facturas";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // --- A completar cuando existan Cliente/Proveedor ---
    // @RequestParam(required = false) String idPersona
    // if (idPersona != null) { facturas = svcFacturaCliente.listarPorCliente(idPersona) ó svcFacturaProveedor.listarPorProveedor(idPersona); }
}
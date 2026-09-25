package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioRegistroProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorProveedor {

    @Autowired
    private ServicioRegistroProveedor svcRegistroProveedor;
    @Autowired
    private ServicioProveedor svcProveedor; // para modificar/eliminar/listar, sin contactos

    @PostMapping("/formulario/proveedor/{id}")
    public String guardar(
            @RequestParam String razonSocial,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefonoFijo,
            @RequestParam(required = false) String telefonoCelular,
            @PathVariable String id, Model model) {
        try {
            if (id.isEmpty()) {
                svcRegistroProveedor.registrarProveedor(razonSocial, email, telefonoFijo, telefonoCelular);
            } else {
                svcProveedor.modificarProveedor(id, razonSocial);
            }
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
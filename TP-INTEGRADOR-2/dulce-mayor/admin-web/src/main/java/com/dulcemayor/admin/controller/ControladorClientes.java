package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioClientes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Listado de clientes registrados desde el sitio institucional (solo lectura). */
@Controller
@RequestMapping("/clientes")
public class ControladorClientes {

    private final ServicioClientes servicioClientes;
    private final GestorSesion gestorSesion;

    public ControladorClientes(ServicioClientes servicioClientes, GestorSesion gestorSesion) {
        this.servicioClientes = servicioClientes;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("clientes", servicioClientes.listar(gestorSesion.actual(request).orElseThrow().getToken()));
        return "clientes";
    }
}

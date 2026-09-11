package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.service.DepartamentoServicio;
import com.ejercicioIntegrador.tiendaderopa.service.DireccionServicio;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;
import com.ejercicioIntegrador.tiendaderopa.service.UsuarioServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Controller
public class PortalControlador {

    private final UsuarioServicio usuarioServicio;
    private final DireccionServicio direccionServicio;
    private final PaisServicio paisServicio;
    private final ProvinciaServicio provinciaServicio;
    private final DepartamentoServicio departamentoServicio;
    private final LocalidadServicio localidadServicio;

    public PortalControlador(UsuarioServicio usuarioServicio,
                             DireccionServicio direccionServicio,
                             PaisServicio paisServicio,
                             ProvinciaServicio provinciaServicio,
                             DepartamentoServicio departamentoServicio,
                             LocalidadServicio localidadServicio) {
        this.usuarioServicio = usuarioServicio;
        this.direccionServicio = direccionServicio;
        this.paisServicio = paisServicio;
        this.provinciaServicio = provinciaServicio;
        this.departamentoServicio = departamentoServicio;
        this.localidadServicio = localidadServicio;
    }

    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_JEFE')")
    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/registrar")
    public String registrar(ModelMap modelo) {
        cargarListasUbicacion(modelo);
        return "register.html";
    }

    @PostMapping("/registro")
    public String registro(
            @RequestParam(required = false) MultipartFile archivo,
            @RequestParam Pais pais,
            @RequestParam Provincia provincia,
            @RequestParam Departamento departamento,
            @RequestParam Localidad localidad,
            @RequestParam String codigoPostal,
            @RequestParam String barrio,
            @RequestParam String direccion,
            @RequestParam(required = false) String manzanaPiso,
            @RequestParam(required = false) String referencia,
            @RequestParam String documento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String clave,
            @RequestParam String clave2,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaNacimiento,
            ModelMap modelo) {

        try {
            Direccion direccionGuardada = direccionServicio.crearDireccion(
                    pais, provincia, departamento, localidad,
                    codigoPostal, barrio, direccion, manzanaPiso, referencia
            );

            usuarioServicio.registrar(
                    direccionGuardada, documento, tipoDocumento, nombre, apellido,
                    email, clave, clave2, fechaNacimiento
            );

            modelo.put("exito", "Usuario registrado correctamente");
            return "index.html";

        } catch (MiException ex) {
            cargarListasUbicacion(modelo);
            modelo.put("error", ex.getMessage());
            modelo.put("documento", documento);
            modelo.put("tipoDocumento", tipoDocumento);
            modelo.put("nombre", nombre);
            modelo.put("apellido", apellido);
            modelo.put("email", email);
            modelo.put("fechaNacimiento", fechaNacimiento);
            modelo.put("codigoPostal", codigoPostal);
            modelo.put("barrio", barrio);
            modelo.put("direccion", direccion);
            modelo.put("manzanaPiso", manzanaPiso);
            modelo.put("referencia", referencia);
            return "register.html";

        } catch (Exception ex) {
            cargarListasUbicacion(modelo);
            modelo.put("error", "Error inesperado del sistema: " + ex.getMessage());
            return "register.html";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "Usuario o contraseña inválidos");
        }
        return "login.html";
    }

        private void cargarListasUbicacion(ModelMap modelo) {
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("provincias", provinciaServicio.listarTodas());
        modelo.addAttribute("departamentos", departamentoServicio.listarTodos());
        modelo.addAttribute("localidades", localidadServicio.listarTodas());
    }
}
package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.OpcionDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioUbicacion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints JSON internos de admin-web para los selects en cascada. El navegador nunca recibe el JWT:
 * consulta a admin-web con su cookie de sesión y admin-web consulta a la API con el token.
 */
@RestController
@RequestMapping("/ubicacion")
public class ControladorUbicacion {

    private final ServicioUbicacion servicioUbicacion;
    private final GestorSesion gestorSesion;

    public ControladorUbicacion(ServicioUbicacion servicioUbicacion, GestorSesion gestorSesion) {
        this.servicioUbicacion = servicioUbicacion;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping("/provincias/{idPais}")
    public List<OpcionDTO> provincias(@PathVariable String idPais, HttpServletRequest request) {
        return servicioUbicacion.listarProvincias(token(request), idPais);
    }

    @GetMapping("/departamentos/{idProvincia}")
    public List<OpcionDTO> departamentos(@PathVariable String idProvincia, HttpServletRequest request) {
        return servicioUbicacion.listarDepartamentos(token(request), idProvincia);
    }

    @GetMapping("/localidades/{idDepartamento}")
    public List<OpcionDTO> localidades(@PathVariable String idDepartamento, HttpServletRequest request) {
        return servicioUbicacion.listarLocalidades(token(request), idDepartamento);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Void> errorDeApi(ApiException error) {
        return ResponseEntity.status(error.getEstado()).build();
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}

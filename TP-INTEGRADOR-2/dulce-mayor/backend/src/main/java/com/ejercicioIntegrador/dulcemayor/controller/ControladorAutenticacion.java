package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.CambioClaveRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CanjeCodigoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LoginRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RegistroClienteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SesionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UsuarioSesionDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioAutenticacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticación. No hereda de BaseController porque no es un recurso CRUD (mismo criterio que el catálogo).
 * El usuario autenticado se toma siempre del token (Authentication), nunca de un dato enviado por el cliente.
 */
@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/login")
    public ResponseEntity<SesionDTO> login(@RequestBody LoginRequestDTO login) throws MiException {
        return ResponseEntity.ok(servicioAutenticacion.login(login));
    }

    /** Registro público de clientes (HU-09). Devuelve el token: el cliente queda autenticado. */
    @PostMapping("/registro")
    public ResponseEntity<SesionDTO> registrarCliente(@RequestBody RegistroClienteDTO registro) throws MiException {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAutenticacion.registrarCliente(registro));
    }

    @GetMapping("/yo")
    public ResponseEntity<UsuarioSesionDTO> usuarioActual(Authentication autenticacion) throws MiException {
        return ResponseEntity.ok(servicioAutenticacion.usuarioActual(autenticacion.getName()));
    }

    /** Cambio de clave obligatorio (HU-06) o voluntario. Devuelve un token nuevo. */
    @PutMapping("/clave")
    public ResponseEntity<SesionDTO> cambiarClave(Authentication autenticacion,
                                                  @RequestBody CambioClaveRequestDTO cambio) throws MiException {
        return ResponseEntity.ok(servicioAutenticacion.cambiarClave(autenticacion.getName(), cambio));
    }

    /** Canje del código de un solo uso del ingreso con Google/Facebook por el token. */
    @PostMapping("/oauth2/canje")
    public ResponseEntity<SesionDTO> canjearCodigo(@RequestBody CanjeCodigoDTO canje) throws MiException {
        return ResponseEntity.ok(servicioAutenticacion.canjearCodigo(canje));
    }
}

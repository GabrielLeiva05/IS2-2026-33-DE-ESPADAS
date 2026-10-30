package com.competencia.blockazo.controller;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.AuthResponse;
import com.competencia.blockazo.model.dto.CambioClaveRequest;
import com.competencia.blockazo.model.dto.MensajeResponse;
import com.competencia.blockazo.model.dto.PerfilRequest;
import com.competencia.blockazo.model.dto.UsuarioResponse;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.service.AuthService;
import com.competencia.blockazo.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seccion "Perfil" de la pagina: el usuario logueado ve y modifica SUS datos.
 * El usuario se toma del JWT, nunca de un id enviado por el cliente.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public PerfilController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    /** Mi perfil: ver mis datos. */
    @GetMapping
    public UsuarioResponse miPerfil(Authentication authentication) throws ErrorServiceException {
        return UsuarioResponse.de(usuarioService.buscarPorCorreo(authentication.getName()));
    }

    /**
     * Modificar todos mis datos (nombre, apellido, DNI, fecha de nacimiento, direccion y correo).
     * Como el correo es el usuario del token, se devuelve un JWT nuevo.
     */
    @PutMapping
    public AuthResponse actualizarPerfil(Authentication authentication,
                                         @Valid @RequestBody PerfilRequest request) throws ErrorServiceException {
        Usuario actualizado = usuarioService.actualizarPerfil(authentication.getName(), request);
        return authService.respuesta(actualizado);
    }

    /** Cambiar mi clave. */
    @PutMapping("/clave")
    public MensajeResponse cambiarClave(Authentication authentication,
                                        @Valid @RequestBody CambioClaveRequest request) throws ErrorServiceException {
        usuarioService.cambiarClave(authentication.getName(), request);
        return new MensajeResponse("La clave se modifico correctamente");
    }
}

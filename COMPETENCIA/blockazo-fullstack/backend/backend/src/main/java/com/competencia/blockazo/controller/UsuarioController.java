package com.competencia.blockazo.controller;

import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ABM de usuarios (solo ADMIN) aplicando el patron Template:
 * TODOS los endpoints se heredan de BaseController.
 * Las reglas de negocio (ej. un admin no puede darse de baja a si mismo)
 * viven unicamente en UsuarioService.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController extends BaseController<Usuario, Long> {

    public UsuarioController(UsuarioService service) {
        super(service);
    }
}

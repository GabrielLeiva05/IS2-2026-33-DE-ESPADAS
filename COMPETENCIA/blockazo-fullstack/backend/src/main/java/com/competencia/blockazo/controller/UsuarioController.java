package com.competencia.blockazo.controller;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ABM de usuarios (solo ADMIN) aplicando el patron Template:
 * TODOS los endpoints se heredan de BaseController; aca solo se redefinen hooks.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController extends BaseController<Usuario, Long> {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService service) {
        super(service);
        this.usuarioService = service;
    }

    /** Hook: un administrador no puede darse de baja a si mismo. */
    @Override
    protected void preUseCase(BaseUseCaseController useCase, Long id, Usuario entidad) throws ErrorServiceException {
        if (useCase == BaseUseCaseController.BAJA) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario logueado = usuarioService.buscarPorCorreo(auth.getName());
            if (logueado.getId().equals(id)) {
                throw new ErrorServiceException("No puede darse de baja a si mismo");
            }
        }
    }
}

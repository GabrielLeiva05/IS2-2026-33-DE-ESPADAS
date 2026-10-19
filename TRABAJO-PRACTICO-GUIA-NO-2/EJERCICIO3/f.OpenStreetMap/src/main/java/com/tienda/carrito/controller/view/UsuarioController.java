package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.UsuarioService;
import com.tienda.carrito.config.SesionUsuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** ABM de Usuarios (solo ADMIN). Completa dos hooks: carga la lista de roles y pasa el usuario logueado a la baja. */
@Controller
@RequestMapping("/admin/usuario")
public class UsuarioController extends BaseController<Usuario, String> {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService service) {
        super(service);
        this.usuarioService = service;
        initController(new Usuario(), "Listado de usuarios", "Usuario");
    }

    @Override
    protected void preUseCase(BaseUseCaseController useCase, Model model, Usuario entity) {
        if (useCase != BaseUseCaseController.LISTAR && useCase != BaseUseCaseController.BAJA) {
            model.addAttribute("roles", Rol.values());
        }
        if (useCase == BaseUseCaseController.ALTA && entity != null && entity.getRol() == null) {
            entity.setRol(Rol.CLIENTE); // valor sugerido en el formulario de alta
        }
    }

    /** La regla "nadie puede eliminarse a si mismo" esta en UsuarioService; aca solo se le informa quien es el actor. */
    @Override
    protected void ejecutarBaja(String id, HttpSession sesion) throws ErrorServiceException {
        String idActor = (String) sesion.getAttribute(SesionUsuario.ID);
        usuarioService.baja(id, idActor);
    }
}

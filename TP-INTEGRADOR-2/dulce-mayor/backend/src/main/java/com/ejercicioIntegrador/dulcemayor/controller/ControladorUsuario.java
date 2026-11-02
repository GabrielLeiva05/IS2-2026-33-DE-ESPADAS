package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.dto.UsuarioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UsuarioResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de cuentas para el jefe administrativo (solo lectura).
 * Las cuentas se crean con el alta de empleados o el registro de clientes y se desactivan con su baja:
 * por eso el paso redefinido operacionHabilitada() solo deja consultar y listar.
 */
@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario extends BaseController<UsuarioRequestDTO, UsuarioResponseDTO> {

    private final ServicioUsuario servicioUsuario;

    public ControladorUsuario(ServicioUsuario servicioUsuario) {
        super(servicioUsuario);
        this.servicioUsuario = servicioUsuario;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso == BaseUseCaseController.LISTAR || casoDeUso == BaseUseCaseController.CONSULTAR;
    }

    @GetMapping("/buscar")
    public ResponseEntity<UsuarioResponseDTO> buscarPorCorreo(@RequestParam String correo) throws MiException {
        return ResponseEntity.ok(servicioUsuario.buscarUsuarioPorNombre(correo));
    }
}

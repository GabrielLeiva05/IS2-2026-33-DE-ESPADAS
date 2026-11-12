package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteCampoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DatosPersonalesDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clientes minoristas.
 *  - Endpoints heredados: listar y consultar (administrativos) y baja (jefe).
 *  - El alta NO se hace acá: es el registro público (/api/auth/registro), que fija el rol CLIENTE.
 *  - La modificación genérica está deshabilitada: cada cliente edita SUS datos en /api/clientes/yo,
 *    identificado por el token y nunca por un id enviado por el cliente.
 */
@RestController
@RequestMapping("/api/clientes")
public class ControladorCliente extends BaseController<ClienteRequestDTO, ClienteResponseDTO> {

    private final ServicioCliente servicioCliente;

    public ControladorCliente(ServicioCliente servicioCliente) {
        super(servicioCliente);
        this.servicioCliente = servicioCliente;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso != BaseUseCaseController.ALTA && casoDeUso != BaseUseCaseController.MODIFICACION;
    }

    /** Alta de un cliente desde la app del preventista: el cliente escribe su correo y su contraseña. */
    @PostMapping("/campo")
    public ResponseEntity<ClienteResponseDTO> altaEnCampo(@RequestBody ClienteCampoDTO alta) throws MiException {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCliente.registrarClienteEnCampo(alta));
    }

    /** Busca un cliente ya registrado (por ejemplo, desde el sitio) para asociarle un punto de venta. */
    @GetMapping("/buscar")
    public ResponseEntity<ClienteResponseDTO> buscarPorCorreo(@RequestParam String correo) throws MiException {
        return ResponseEntity.ok(servicioCliente.buscarClientePorCorreo(correo));
    }

    @GetMapping("/yo")
    public ResponseEntity<ClienteResponseDTO> misDatos(Authentication autenticacion) throws MiException {
        return ResponseEntity.ok(servicioCliente.consultarMisDatos(autenticacion.getName()));
    }

    @PutMapping("/yo")
    public ResponseEntity<ClienteResponseDTO> actualizarMisDatos(Authentication autenticacion,
                                                                 @RequestBody DatosPersonalesDTO datos) throws MiException {
        return ResponseEntity.ok(servicioCliente.actualizarDatosPersonales(autenticacion.getName(), datos));
    }
}

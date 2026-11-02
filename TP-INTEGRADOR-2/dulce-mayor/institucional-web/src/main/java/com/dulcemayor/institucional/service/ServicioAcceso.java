package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.CambioClaveFormularioDTO;
import com.dulcemayor.institucional.dto.CambioClaveSolicitudDTO;
import com.dulcemayor.institucional.dto.CanjeSolicitudDTO;
import com.dulcemayor.institucional.dto.LoginFormularioDTO;
import com.dulcemayor.institucional.dto.LoginSolicitudDTO;
import com.dulcemayor.institucional.dto.RegistroFormularioDTO;
import com.dulcemayor.institucional.dto.RegistroSolicitudDTO;
import com.dulcemayor.institucional.dto.SesionApiDTO;
import com.dulcemayor.institucional.seguridad.SesionCliente;
import org.springframework.stereotype.Service;

/**
 * Acceso de clientes: registro (HU-09), ingreso con correo y contraseña, canje del código que deja
 * el ingreso con Google/Facebook y cambio voluntario de contraseña.
 * Las cuentas de empleados no abren sesión en el sitio institucional.
 */
@Service
public class ServicioAcceso {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final ClienteApi clienteApi;

    public ServicioAcceso(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public SesionCliente ingresar(LoginFormularioDTO formulario) {
        return sesionDeCliente(clienteApi.enviar(null,
                new LoginSolicitudDTO(formulario.getCorreo(), formulario.getClave()),
                SesionApiDTO.class, "/api/auth/login"));
    }

    /** El cliente elige su contraseña y no queda obligado a cambiarla. La API asigna siempre el rol CLIENTE. */
    public SesionCliente registrar(RegistroFormularioDTO f) {
        RegistroSolicitudDTO solicitud = new RegistroSolicitudDTO(texto(f.getNombre()), texto(f.getApellido()),
                texto(f.getCorreo()), f.getClave(), f.getConfirmacionClave(),
                texto(f.getNumeroDocumento()) == null ? null : texto(f.getTipoDocumento()),
                texto(f.getNumeroDocumento()), texto(f.getFechaNacimiento()));
        return sesionDeCliente(clienteApi.enviar(null, solicitud, SesionApiDTO.class, "/api/auth/registro"));
    }

    /** Segundo paso del ingreso con Google/Facebook: el código de un solo uso se cambia por el token. */
    public SesionCliente canjearCodigo(String codigo) {
        return sesionDeCliente(clienteApi.enviar(null, new CanjeSolicitudDTO(codigo), SesionApiDTO.class,
                "/api/auth/oauth2/canje"));
    }

    public SesionCliente cambiarClave(SesionCliente actual, CambioClaveFormularioDTO formulario) {
        return sesionDeCliente(clienteApi.reemplazar(actual.getToken(),
                new CambioClaveSolicitudDTO(formulario.getClaveActual(), formulario.getNuevaClave(),
                        formulario.getConfirmarClave()),
                SesionApiDTO.class, "/api/auth/clave"));
    }

    private static SesionCliente sesionDeCliente(SesionApiDTO sesion) {
        if (sesion == null || sesion.getUsuario() == null || !ROL_CLIENTE.equals(sesion.getUsuario().getRol())) {
            throw new ApiException(403,
                    "Las cuentas del personal ingresan desde el sistema administrativo de Dulce Mayor.", null);
        }
        return SesionCliente.desde(sesion);
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

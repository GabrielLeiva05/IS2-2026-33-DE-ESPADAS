package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.CambioClaveFormularioDTO;
import com.dulcemayor.admin.dto.CambioClaveSolicitudDTO;
import com.dulcemayor.admin.dto.LoginFormularioDTO;
import com.dulcemayor.admin.dto.LoginSolicitudDTO;
import com.dulcemayor.admin.dto.Roles;
import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;

/** Ingreso de empleados con correo y contraseña, y cambio de contraseña (HU-06). */
@Service
public class ServicioAcceso {

    private final ClienteApi clienteApi;

    public ServicioAcceso(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    /**
     * Autentica contra la API. Las cuentas de cliente no ingresan al sistema administrativo:
     * aunque la API las autentique, aquí no se abre sesión.
     */
    public SesionUsuario ingresar(LoginFormularioDTO formulario) {
        SesionApiDTO sesion = clienteApi.enviar(null,
                new LoginSolicitudDTO(formulario.getCorreo(), formulario.getClave()),
                SesionApiDTO.class, "/api/auth/login");
        if (sesion == null || sesion.getUsuario() == null || Roles.CLIENTE.equals(sesion.getUsuario().getRol())) {
            throw new ApiException(403,
                    "Esta cuenta es de cliente: ingresá desde el sitio de Dulce Mayor.", null);
        }
        return SesionUsuario.desde(sesion);
    }

    /** Cambia la contraseña y devuelve la sesión con el token nuevo (ya sin la restricción de clave temporal). */
    public SesionUsuario cambiarClave(SesionUsuario actual, CambioClaveFormularioDTO formulario) {
        SesionApiDTO sesion = clienteApi.reemplazar(actual.getToken(),
                new CambioClaveSolicitudDTO(formulario.getClaveActual(), formulario.getNuevaClave(),
                        formulario.getConfirmarClave()),
                SesionApiDTO.class, "/api/auth/clave");
        return SesionUsuario.desde(sesion);
    }
}

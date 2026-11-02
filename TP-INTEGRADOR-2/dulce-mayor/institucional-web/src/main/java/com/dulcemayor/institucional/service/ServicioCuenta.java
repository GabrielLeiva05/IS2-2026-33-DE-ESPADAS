package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.ClienteDTO;
import com.dulcemayor.institucional.dto.DatosPersonalesFormularioDTO;
import com.dulcemayor.institucional.dto.DatosPersonalesSolicitudDTO;
import org.springframework.stereotype.Service;

/** "Mi cuenta": el cliente consulta y edita solo sus datos personales (nunca su correo ni su rol). */
@Service
public class ServicioCuenta {

    private final ClienteApi clienteApi;

    public ServicioCuenta(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public ClienteDTO misDatos(String token) {
        return clienteApi.obtener(token, ClienteDTO.class, "/api/clientes/yo");
    }

    public ClienteDTO actualizarDatos(String token, DatosPersonalesFormularioDTO f) {
        DatosPersonalesSolicitudDTO solicitud = new DatosPersonalesSolicitudDTO(texto(f.getNombre()),
                texto(f.getApellido()), texto(f.getFechaNacimiento()),
                texto(f.getNumeroDocumento()) == null ? null : texto(f.getTipoDocumento()),
                texto(f.getNumeroDocumento()));
        return clienteApi.reemplazar(token, solicitud, ClienteDTO.class, "/api/clientes/yo");
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

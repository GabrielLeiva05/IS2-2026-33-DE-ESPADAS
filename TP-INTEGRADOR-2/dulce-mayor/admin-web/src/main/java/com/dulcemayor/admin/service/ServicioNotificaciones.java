package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.CorreoAdminDTO;
import com.dulcemayor.admin.dto.SuscripcionAdminDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Registro de correos y suscripciones al newsletter. */
@Service
public class ServicioNotificaciones {

    private final ClienteApi clienteApi;

    public ServicioNotificaciones(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<CorreoAdminDTO> correos(String token) {
        return clienteApi.listar(token, CorreoAdminDTO[].class, "/api/notificaciones/correos");
    }

    public List<SuscripcionAdminDTO> suscripciones(String token) {
        return clienteApi.listar(token, SuscripcionAdminDTO[].class, "/api/notificaciones/newsletter");
    }

    /** Dispara el envío mensual del newsletter sin esperar al día programado. */
    public int enviarNewsletter(String token) {
        var respuesta = clienteApi.enviarSinCuerpo(token, java.util.Map.class,
                "/api/notificaciones/newsletter/enviar");
        Object enviados = respuesta == null ? null : respuesta.get("enviados");
        return enviados instanceof Number numero ? numero.intValue() : 0;
    }
}

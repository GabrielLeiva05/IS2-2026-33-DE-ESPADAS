package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.PuntoVentaAdminDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consulta de todos los puntos de venta de la empresa. */
@Service
public class ServicioPuntosVenta {

    private final ClienteApi clienteApi;

    public ServicioPuntosVenta(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PuntoVentaAdminDTO> listar(String token) {
        return clienteApi.listar(token, PuntoVentaAdminDTO[].class, "/api/puntos-venta");
    }
}

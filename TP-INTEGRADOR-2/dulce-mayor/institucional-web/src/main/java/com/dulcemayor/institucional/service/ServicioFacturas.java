package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.FacturaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facturas del cliente que inició sesión (HU-30). La API identifica al cliente por el token: una factura ajena
 * responde 404 aunque se cambie el id en la URL.
 */
@Service
public class ServicioFacturas {

    private static final String BASE = "/api/facturas/mis-facturas";

    private final ClienteApi clienteApi;

    public ServicioFacturas(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<FacturaDTO> misFacturas(String token) {
        return clienteApi.listar(token, FacturaDTO[].class, BASE);
    }

    public FacturaDTO miFactura(String token, String id) {
        return clienteApi.obtener(token, FacturaDTO.class, BASE + "/{id}", id);
    }

    public byte[] pdf(String token, String id) {
        return clienteApi.obtenerArchivo(token, BASE + "/{id}/pdf", id);
    }
}

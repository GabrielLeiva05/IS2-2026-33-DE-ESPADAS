package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.ClienteDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Consulta de clientes registrados (solo lectura para administración). */
@Service
public class ServicioClientes {

    private final ClienteApi clienteApi;

    public ServicioClientes(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<ClienteDTO> listar(String token) {
        return clienteApi.listar(token, ClienteDTO[].class, "/api/clientes").stream()
                .sorted(Comparator.comparing(ClienteDTO::getApellido, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }
}

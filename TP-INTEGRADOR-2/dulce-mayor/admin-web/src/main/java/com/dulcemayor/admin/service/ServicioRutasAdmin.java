package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.PedidoAdminDTO;
import com.dulcemayor.admin.dto.RutaAdminDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Asignación de pedidos a hojas de ruta y consulta de la jornada. */
@Service
public class ServicioRutasAdmin {

    private final ClienteApi clienteApi;

    public ServicioRutasAdmin(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<RutaAdminDTO> listar(String token, String fecha) {
        return clienteApi.listarConFiltros(token, RutaAdminDTO[].class, "/api/rutas",
                new java.util.LinkedHashMap<>(java.util.Map.of("fecha", fecha == null ? "" : fecha)));
    }

    public List<EmpleadoDTO> distribuidores(String token) {
        return clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados/rol/{rol}", "DISTRIBUIDOR");
    }

    /** Pedidos que todavía se pueden sumar a una hoja de ruta. */
    public List<PedidoAdminDTO> pedidosAsignables(String token) {
        return clienteApi.listar(token, PedidoAdminDTO[].class, "/api/pedidos").stream()
                .filter(pedido -> "CONFIRMADO".equals(pedido.getEstado()) || "COMPLETADO".equals(pedido.getEstado()))
                .toList();
    }

    public RutaAdminDTO asignar(String token, String fecha, String idDistribuidor, List<String> idsPedidos,
                                String observacion) {
        return clienteApi.enviar(token, new AsignacionForm(fecha, idDistribuidor, idsPedidos, observacion),
                RutaAdminDTO.class, "/api/rutas");
    }

    record AsignacionForm(String fecha, String idDistribuidor, List<String> idsPedidos, String observacion) {
    }
}

package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.AsignacionFormularioDTO;
import com.dulcemayor.admin.dto.AsignacionSolicitudDTO;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.PedidoDTO;
import com.dulcemayor.admin.dto.Roles;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Asignación de pedidos confirmados (PENDIENTE_DE_ENTREGA) a un distribuidor. Con esos pedidos la app del
 * distribuidor arma su ruta de entregas del día; los que no se entregan siguen asignados para el día siguiente.
 */
@Service
public class ServicioEntregas {

    private final ClienteApi clienteApi;

    public ServicioEntregas(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PedidoDTO> pendientesDeEntrega(String token) {
        return clienteApi.listar(token, PedidoDTO[].class, "/api/pedidos/estado/{estado}", "PENDIENTE_DE_ENTREGA");
    }

    public List<PedidoDTO> sinAsignar(List<PedidoDTO> pendientes) {
        return pendientes.stream().filter(p -> p.getIdDistribuidor() == null).toList();
    }

    public List<PedidoDTO> asignados(List<PedidoDTO> pendientes) {
        return pendientes.stream().filter(p -> p.getIdDistribuidor() != null).toList();
    }

    public List<EmpleadoDTO> distribuidores(String token) {
        return clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados/rol/{rol}", Roles.DISTRIBUIDOR);
    }

    public int asignar(String token, AsignacionFormularioDTO formulario) {
        PedidoDTO[] asignados = clienteApi.enviar(token, new AsignacionSolicitudDTO(formulario.getIdDistribuidor(),
                formulario.getIdsPedido()), PedidoDTO[].class, "/api/pedidos/asignacion");
        return asignados == null ? 0 : asignados.length;
    }
}

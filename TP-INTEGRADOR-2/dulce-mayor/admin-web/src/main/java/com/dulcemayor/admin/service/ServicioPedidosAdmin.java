package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.PagoAdminDTO;
import com.dulcemayor.admin.dto.PedidoAdminDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Bandeja de pedidos y conciliación de pagos (todo pasa por la API; acá no hay reglas de negocio). */
@Service
public class ServicioPedidosAdmin {

    private final ClienteApi clienteApi;

    public ServicioPedidosAdmin(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PedidoAdminDTO> listar(String token) {
        return clienteApi.listar(token, PedidoAdminDTO[].class, "/api/pedidos");
    }

    /** Filtra en memoria por estado y por texto del comercio (la API ya devuelve los más recientes primero). */
    public List<PedidoAdminDTO> listar(String token, String estado, String busqueda) {
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase();
        return listar(token).stream()
                .filter(pedido -> estado == null || estado.isBlank() || estado.equals(pedido.getEstado()))
                .filter(pedido -> texto.isEmpty() || (pedido.getNombreComercial() != null
                        && pedido.getNombreComercial().toLowerCase().contains(texto)))
                .sorted(Comparator.comparing(PedidoAdminDTO::getFechaReferencia).reversed())
                .toList();
    }

    public PedidoAdminDTO detalle(String token, String idPedido) {
        return clienteApi.obtener(token, PedidoAdminDTO.class, "/api/pedidos/{id}", idPedido);
    }

    public List<PagoAdminDTO> pagos(String token, String idPedido) {
        return clienteApi.listar(token, PagoAdminDTO[].class, "/api/pagos/pedido/{id}", idPedido);
    }

    public PagoAdminDTO acreditar(String token, String idPago, String referencia, String observacion) {
        return clienteApi.enviar(token, new ConfirmacionForm(referencia, observacion), PagoAdminDTO.class,
                "/api/pagos/{id}/acreditar", idPago);
    }

    /** Inicia un cobro (transferencia, Mercado Pago o efectivo) desde la bandeja. */
    public PagoAdminDTO iniciarPago(String token, String idPedido, String medio) {
        return clienteApi.enviar(token, new InicioForm(medio), PagoAdminDTO.class,
                "/api/pedidos/{id}/pagos", idPedido);
    }

    public PagoAdminDTO rechazar(String token, String idPago, String observacion) {
        return clienteApi.enviar(token, new ConfirmacionForm(null, observacion), PagoAdminDTO.class,
                "/api/pagos/{id}/rechazar", idPago);
    }

    /** Cobro en efectivo registrado por administración (interino hasta la app del distribuidor). */
    public PagoAdminDTO registrarEfectivo(String token, String idPedido) {
        return clienteApi.enviarSinCuerpo(token, PagoAdminDTO.class, "/api/pedidos/{id}/pagos/efectivo", idPedido);
    }

    public PedidoAdminDTO cancelar(String token, String idPedido) {
        return clienteApi.enviarSinCuerpo(token, PedidoAdminDTO.class, "/api/pedidos/{id}/cancelar", idPedido);
    }

    public String comprobanteHtml(String token, String idPedido) {
        return clienteApi.obtenerTexto(token, "/api/pedidos/{id}/comprobante", idPedido);
    }

    /** La API informa 400 cuando el pedido todavía no tiene comprobante. */
    public boolean tieneComprobante(String token, String idPedido) {
        try {
            clienteApi.obtenerTexto(token, "/api/pedidos/{id}/comprobante", idPedido);
            return true;
        } catch (ApiException error) {
            return false;
        }
    }

    /** Cuerpo de la conciliación: referencia externa y observación son opcionales. */
    record ConfirmacionForm(String referencia, String observacion) {
    }

    /** Cuerpo del inicio de cobro. */
    record InicioForm(String medio) {
    }
}

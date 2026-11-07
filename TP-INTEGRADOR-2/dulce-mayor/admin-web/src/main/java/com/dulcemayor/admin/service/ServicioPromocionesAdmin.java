package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.PromocionAdminDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** Administración de códigos promocionales. */
@Service
public class ServicioPromocionesAdmin {

    private final ClienteApi clienteApi;

    public ServicioPromocionesAdmin(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PromocionAdminDTO> listar(String token) {
        return clienteApi.listar(token, PromocionAdminDTO[].class, "/api/promociones");
    }

    public PromocionAdminDTO alta(String token, String codigo, String descripcion, String tipo, BigDecimal valor,
                                  String fechaDesde, String fechaHasta, boolean activa, boolean usoUnico,
                                  BigDecimal minimoCompra) {
        return clienteApi.enviar(token, new PromocionForm(codigo, descripcion, tipo, valor,
                vacio(fechaDesde), vacio(fechaHasta), activa, usoUnico, minimoCompra), PromocionAdminDTO.class,
                "/api/promociones");
    }

    public void baja(String token, String id) {
        clienteApi.eliminar(token, "/api/promociones/{id}", id);
    }

    private static String vacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    record PromocionForm(String codigo, String descripcion, String tipo, BigDecimal valor, String fechaDesde,
                         String fechaHasta, Boolean activa, Boolean usoUnico, BigDecimal minimoCompra) {
    }
}

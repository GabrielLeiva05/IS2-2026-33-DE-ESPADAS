package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.CotizacionDTO;
import com.dulcemayor.institucional.dto.SucursalDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** Datos públicos del sitio: sucursales y cotización informativa. */
@Service
public class ServicioSitio {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioSitio.class);

    private final ClienteApi clienteApi;

    public ServicioSitio(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<SucursalDTO> sucursales() {
        try {
            return clienteApi.listar(null, SucursalDTO[].class, "/api/sucursales");
        } catch (ApiException e) {
            LOG.warn("No se pudieron cargar las sucursales: {}", e.getMessage());
            return List.of();
        }
    }

    /** Devuelve null si la API no responde: el sitio sigue funcionando sin la cotización. */
    public CotizacionDTO cotizacion() {
        try {
            return clienteApi.obtener(null, CotizacionDTO.class, "/api/cotizacion-dolar");
        } catch (ApiException e) {
            LOG.warn("No se pudo cargar la cotización: {}", e.getMessage());
            return null;
        }
    }
}

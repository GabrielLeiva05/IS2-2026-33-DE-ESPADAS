package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.PuntoVentaDTO;
import com.dulcemayor.admin.dto.Roles;
import com.dulcemayor.admin.dto.RutaDTO;
import com.dulcemayor.admin.dto.RutaFormularioDTO;
import com.dulcemayor.admin.dto.RutaSolicitudDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HU-18: el personal administrativo arma la ruta diaria de cada preventista (preventista, fecha y puntos).
 * La API valida una ruta por preventista y día, fechas y puntos; la app optimiza el orden desde la ubicación del preventista (OSRM + optimizador en la API).
 */
@Service
public class ServicioRutas {

    private final ClienteApi clienteApi;

    public ServicioRutas(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<RutaDTO> listarPorFecha(String token, String fecha) {
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("fecha", fecha);
        return clienteApi.listarConFiltros(token, RutaDTO[].class, "/api/rutas/fecha", filtros).stream()
                .sorted(Comparator.comparing(RutaDTO::getTipoRuta).thenComparing(RutaDTO::getNombreEmpleado,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public RutaDTO crear(String token, RutaFormularioDTO formulario) {
        String fecha = formulario.getFecha() == null || formulario.getFecha().isBlank()
                ? LocalDate.now().toString() : formulario.getFecha();
        return clienteApi.enviar(token, new RutaSolicitudDTO(fecha, "VISITA", formulario.getIdPreventista(),
                formulario.getIdsPuntoVenta()), RutaDTO.class, "/api/rutas");
    }

    public void darDeBaja(String token, String id) {
        clienteApi.eliminar(token, "/api/rutas/{id}", id);
    }

    public List<EmpleadoDTO> preventistas(String token) {
        return clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados/rol/{rol}", Roles.PREVENTISTA);
    }

    public List<PuntoVentaDTO> puntosDeVenta(String token) {
        return clienteApi.listar(token, PuntoVentaDTO[].class, "/api/puntos-venta").stream()
                .sorted(Comparator.comparing(PuntoVentaDTO::getNombreFantasia, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

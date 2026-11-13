package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.ArchivoDescargaDTO;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.FiltroReporteFormularioDTO;
import com.dulcemayor.admin.dto.OpcionDTO;
import com.dulcemayor.admin.dto.ReporteClientesPedidosDTO;
import com.dulcemayor.admin.dto.ReporteVentasDTO;
import com.dulcemayor.admin.dto.Roles;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes del panel administrativo. Todo se resuelve en la API (validación de fechas, armado, totales y
 * generación de PDF/Excel): este servicio solo traduce el formulario a parámetros, pide los datos o el archivo
 * con el token de la sesión y arma las listas de los filtros. No aplica reglas de negocio.
 */
@Service
public class ServicioReportes {

    private static final String CLIENTES_PEDIDOS = "/api/reportes/clientes-pedidos";
    private static final String VENTAS = "/api/reportes/ventas";

    private final ClienteApi clienteApi;

    public ServicioReportes(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    // ---------------- Clientes y pedidos ----------------

    public ReporteClientesPedidosDTO clientesPedidos(String token, FiltroReporteFormularioDTO filtro) {
        return clienteApi.obtenerConFiltros(token, ReporteClientesPedidosDTO.class, CLIENTES_PEDIDOS,
                parametrosClientesPedidos(filtro));
    }

    public ArchivoDescargaDTO descargarClientesPedidos(String token, FiltroReporteFormularioDTO filtro,
                                                       String formato) {
        Map<String, String> parametros = parametrosClientesPedidos(filtro);
        parametros.put("formato", formato);
        return clienteApi.descargar(token, CLIENTES_PEDIDOS + "/exportar", parametros);
    }

    private Map<String, String> parametrosClientesPedidos(FiltroReporteFormularioDTO filtro) {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("desde", filtro.getDesde());
        parametros.put("hasta", filtro.getHasta());
        parametros.put("idDepartamento", filtro.getIdDepartamento());
        parametros.put("estado", filtro.getEstado());
        return parametros;
    }

    // ---------------- Histórico de ventas ----------------

    public ReporteVentasDTO ventas(String token, FiltroReporteFormularioDTO filtro) {
        return clienteApi.obtenerConFiltros(token, ReporteVentasDTO.class, VENTAS, parametrosVentas(filtro));
    }

    public ArchivoDescargaDTO descargarVentas(String token, FiltroReporteFormularioDTO filtro, String formato) {
        Map<String, String> parametros = parametrosVentas(filtro);
        parametros.put("formato", formato);
        return clienteApi.descargar(token, VENTAS + "/exportar", parametros);
    }

    private Map<String, String> parametrosVentas(FiltroReporteFormularioDTO filtro) {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("desde", filtro.getDesde());
        parametros.put("hasta", filtro.getHasta());
        parametros.put("idDepartamento", filtro.getIdDepartamento());
        parametros.put("idPreventista", filtro.getIdPreventista());
        return parametros;
    }

    // ---------------- Listas de los filtros ----------------

    public List<OpcionDTO> departamentos(String token) {
        return clienteApi.listar(token, OpcionDTO[].class, "/api/departamentos");
    }

    public List<EmpleadoDTO> preventistas(String token) {
        return clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados/rol/{rol}", Roles.PREVENTISTA);
    }

    /** Estados de pedido que acepta el filtro (el código lo define la API; aquí solo se les da un nombre legible). */
    public List<OpcionDTO> estadosDePedido() {
        return List.of(opcion("CARGADO", "Cargado"), opcion("PENDIENTE_DE_ENTREGA", "Pendiente de entrega"),
                opcion("ENTREGADO", "Entregado"), opcion("ANULADO", "Anulado"));
    }

    private static OpcionDTO opcion(String id, String nombre) {
        OpcionDTO opcion = new OpcionDTO();
        opcion.setId(id);
        opcion.setNombre(nombre);
        return opcion;
    }
}

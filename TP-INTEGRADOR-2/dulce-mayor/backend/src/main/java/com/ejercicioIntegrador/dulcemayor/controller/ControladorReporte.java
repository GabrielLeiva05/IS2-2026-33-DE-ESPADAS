package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.ArchivoReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.FiltroReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exportacion.FormatoExportacion;
import com.ejercicioIntegrador.dulcemayor.service.ServicioReporte;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Reportes administrativos (solo el jefe administrativo: ver SecurityConfig). Es de solo lectura y no administra
 * una entidad, por eso no hereda de BaseController (mismo criterio que ControladorCatalogo).
 * Recibe los filtros, delega todo en ServicioReporte y arma la respuesta HTTP; no valida reglas ni elige formatos.
 *  - GET /clientes-pedidos y /ventas: datos para mostrar en pantalla.
 *  - GET /clientes-pedidos/exportar y /ventas/exportar?formato=PDF|EXCEL: descarga del archivo con los mismos filtros.
 */
@RestController
@RequestMapping("/api/reportes")
public class ControladorReporte {

    private final ServicioReporte servicioReporte;

    public ControladorReporte(ServicioReporte servicioReporte) {
        this.servicioReporte = servicioReporte;
    }

    @GetMapping("/clientes-pedidos")
    public ResponseEntity<ReporteClientesPedidosDTO> clientesPedidos(
            Authentication autenticacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String idDepartamento,
            @RequestParam(required = false) EstadoPedido estado) throws MiException {
        FiltroReporteDTO filtro = new FiltroReporteDTO(desde, hasta, idDepartamento, null, estado);
        return ResponseEntity.ok(servicioReporte.generarReporteClientesPedidos(filtro, autenticacion.getName()));
    }

    @GetMapping("/clientes-pedidos/exportar")
    public ResponseEntity<byte[]> exportarClientesPedidos(
            Authentication autenticacion,
            @RequestParam FormatoExportacion formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String idDepartamento,
            @RequestParam(required = false) EstadoPedido estado) throws MiException {
        FiltroReporteDTO filtro = new FiltroReporteDTO(desde, hasta, idDepartamento, null, estado);
        return descarga(servicioReporte.exportarClientesPedidos(filtro, formato, autenticacion.getName()));
    }

    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasDTO> ventas(
            Authentication autenticacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String idDepartamento,
            @RequestParam(required = false) String idPreventista) throws MiException {
        FiltroReporteDTO filtro = new FiltroReporteDTO(desde, hasta, idDepartamento, idPreventista, null);
        return ResponseEntity.ok(servicioReporte.generarReporteVentas(filtro, autenticacion.getName()));
    }

    @GetMapping("/ventas/exportar")
    public ResponseEntity<byte[]> exportarVentas(
            Authentication autenticacion,
            @RequestParam FormatoExportacion formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String idDepartamento,
            @RequestParam(required = false) String idPreventista) throws MiException {
        FiltroReporteDTO filtro = new FiltroReporteDTO(desde, hasta, idDepartamento, idPreventista, null);
        return descarga(servicioReporte.exportarVentas(filtro, formato, autenticacion.getName()));
    }

    private ResponseEntity<byte[]> descarga(ArchivoReporteDTO archivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .body(archivo.contenido());
    }
}

package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.ArchivoDescargaDTO;
import com.dulcemayor.admin.dto.FiltroReporteFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioReportes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

/**
 * Reportes del jefe administrativo: clientes y pedidos (pantalla, PDF y Excel) e histórico de ventas (pantalla y
 * Excel). Recibe los filtros del formulario y los envía al servicio, que consulta a la API; no tiene reglas de
 * negocio. Un filtro inválido (por ejemplo, fecha de inicio posterior a la de fin) se muestra en la misma
 * pantalla con el mensaje que devuelve la API.
 */
@Controller
@RequestMapping("/reportes")
public class ControladorReportes {

    private static final String PDF = "PDF";
    private static final String EXCEL = "EXCEL";

    private final ServicioReportes servicioReportes;
    private final GestorSesion gestorSesion;

    public ControladorReportes(ServicioReportes servicioReportes, GestorSesion gestorSesion) {
        this.servicioReportes = servicioReportes;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String inicio() {
        return "redirect:/reportes/clientes-pedidos";
    }

    // ---------------- Clientes y pedidos ----------------

    @GetMapping("/clientes-pedidos")
    public String clientesPedidos(@ModelAttribute("filtro") FiltroReporteFormularioDTO filtro,
                                  HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("departamentos", servicioReportes.departamentos(token));
        model.addAttribute("estados", servicioReportes.estadosDePedido());
        try {
            model.addAttribute("reporte", servicioReportes.clientesPedidos(token, filtro));
        } catch (ApiException e) {
            informarErrorDeFiltros(e, model);
        }
        return "reportes/clientes-pedidos";
    }

    @GetMapping("/clientes-pedidos/pdf")
    public ResponseEntity<byte[]> clientesPedidosPdf(@ModelAttribute FiltroReporteFormularioDTO filtro,
                                                     HttpServletRequest request) {
        return descarga(servicioReportes.descargarClientesPedidos(token(request), filtro, PDF));
    }

    @GetMapping("/clientes-pedidos/excel")
    public ResponseEntity<byte[]> clientesPedidosExcel(@ModelAttribute FiltroReporteFormularioDTO filtro,
                                                       HttpServletRequest request) {
        return descarga(servicioReportes.descargarClientesPedidos(token(request), filtro, EXCEL));
    }

    // ---------------- Histórico de ventas ----------------

    /** La consulta se hace al enviar el formulario (parámetro "consultar"); la primera vez solo se muestran los filtros. */
    @GetMapping("/ventas")
    public String ventas(@ModelAttribute("filtro") FiltroReporteFormularioDTO filtro,
                         @RequestParam(required = false) String consultar, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("departamentos", servicioReportes.departamentos(token));
        model.addAttribute("preventistas", servicioReportes.preventistas(token));
        if (consultar != null) {
            try {
                model.addAttribute("reporte", servicioReportes.ventas(token, filtro));
            } catch (ApiException e) {
                informarErrorDeFiltros(e, model);
            }
        }
        return "reportes/ventas";
    }

    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> ventasExcel(@ModelAttribute FiltroReporteFormularioDTO filtro,
                                              HttpServletRequest request) {
        return descarga(servicioReportes.descargarVentas(token(request), filtro, EXCEL));
    }

    // ---------------- Auxiliares ----------------

    /** Los errores que el usuario puede corregir se muestran en la pantalla; el resto los maneja ManejadorErroresWeb. */
    private void informarErrorDeFiltros(ApiException error, Model model) {
        if (!error.esErrorDeDatos()) {
            throw error;
        }
        model.addAttribute("error", error.getMessage());
    }

    private ResponseEntity<byte[]> descarga(ArchivoDescargaDTO archivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .body(archivo.contenido());
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}

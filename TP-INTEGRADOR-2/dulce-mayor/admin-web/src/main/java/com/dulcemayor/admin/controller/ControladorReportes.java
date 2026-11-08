package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioReportes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Reportes de ventas con filtros, exportación CSV y estadísticas para el panel. */
@Controller
@RequestMapping("/reportes")
public class ControladorReportes {

    private static final List<String> AGRUPACIONES = List.of("departamento", "preventista", "cliente", "estado");

    private final ServicioReportes servicioReportes;
    private final GestorSesion gestorSesion;

    public ControladorReportes(ServicioReportes servicioReportes, GestorSesion gestorSesion) {
        this.servicioReportes = servicioReportes;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String ver(@RequestParam(required = false) String desde,
                      @RequestParam(required = false) String hasta,
                      @RequestParam(defaultValue = "departamento") String agrupacion,
                      HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("reporte", servicioReportes.ventas(token, desde, hasta, agrupacion));
        model.addAttribute("serie", servicioReportes.pedidosPorPreventista(token, desde, hasta, false));
        model.addAttribute("stock", servicioReportes.stock(token));
        model.addAttribute("agrupaciones", AGRUPACIONES);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "reportes/index";
    }

    @GetMapping("/ventas.csv")
    public ResponseEntity<byte[]> exportar(@RequestParam(required = false) String desde,
                                           @RequestParam(required = false) String hasta,
                                           @RequestParam(defaultValue = "departamento") String agrupacion,
                                           HttpServletRequest request) {
        byte[] csv = servicioReportes.ventasCsv(token(request), desde, hasta, agrupacion);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ventas.csv\"")
                .body(csv);
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}

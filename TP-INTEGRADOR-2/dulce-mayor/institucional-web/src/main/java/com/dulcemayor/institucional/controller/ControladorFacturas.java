package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.dto.FacturaDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.service.ServicioFacturas;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** "Mis facturas" (HU-30): listado y descarga en PDF de las facturas del cliente autenticado. */
@Controller
@RequestMapping("/mis-facturas")
public class ControladorFacturas {

    private final ServicioFacturas servicioFacturas;
    private final GestorSesion gestorSesion;

    public ControladorFacturas(ServicioFacturas servicioFacturas, GestorSesion gestorSesion) {
        this.servicioFacturas = servicioFacturas;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String lista(HttpServletRequest request, Model model) {
        model.addAttribute("facturas", servicioFacturas.misFacturas(token(request)));
        return "facturas/lista";
    }

    /** El PDF se pide a la API con el token de la sesión; el navegador nunca ve el token. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String id, HttpServletRequest request) {
        String token = token(request);
        FacturaDTO factura = servicioFacturas.miFactura(token, id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("Factura_DulceMayor_" + factura.getNumeroFactura() + ".pdf").build().toString())
                .body(servicioFacturas.pdf(token, id));
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}

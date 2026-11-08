package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.FacturaDTO;
import com.dulcemayor.admin.dto.FacturaProveedorFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioFacturas;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Facturas (Ola 3) para ADMINISTRATIVO y JEFE: listados de clientes y proveedores, descarga en PDF y carga de
 * facturas de proveedor. Los PDF se piden a la API con el token del usuario (el navegador nunca ve el token).
 */
@Controller
@RequestMapping("/facturas")
public class ControladorFacturas {

    private final ServicioFacturas servicioFacturas;
    private final GestorSesion gestorSesion;

    public ControladorFacturas(ServicioFacturas servicioFacturas, GestorSesion gestorSesion) {
        this.servicioFacturas = servicioFacturas;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "clientes") String tipo, HttpServletRequest request, Model model) {
        boolean proveedores = "proveedores".equals(tipo);
        model.addAttribute("tipo", proveedores ? "proveedores" : "clientes");
        model.addAttribute("facturas", proveedores ? servicioFacturas.facturasDeProveedores(token(request))
                : servicioFacturas.facturasDeClientes(token(request)));
        return "facturas/lista";
    }

    @GetMapping("/clientes/{id}/pdf")
    public ResponseEntity<byte[]> pdfCliente(@PathVariable String id, HttpServletRequest request) {
        return pdf(servicioFacturas.pdfCliente(token(request), id), "Factura_DulceMayor_" + id + ".pdf");
    }

    @GetMapping("/proveedores/{id}/pdf")
    public ResponseEntity<byte[]> pdfProveedor(@PathVariable String id, HttpServletRequest request) {
        return pdf(servicioFacturas.pdfProveedor(token(request), id), "FacturaProveedor_" + id + ".pdf");
    }

    @GetMapping("/proveedores/nueva")
    public String nueva(HttpServletRequest request, Model model) {
        FacturaProveedorFormularioDTO formulario = new FacturaProveedorFormularioDTO();
        formulario.setFechaFactura(LocalDate.now().toString());
        return formulario(formulario, null, request, model);
    }

    @PostMapping("/proveedores")
    public String cargar(@ModelAttribute("formulario") FacturaProveedorFormularioDTO formulario, HttpServletRequest request,
                         Model model, RedirectAttributes redireccion) {
        try {
            FacturaDTO factura = servicioFacturas.cargarFacturaProveedor(token(request), formulario);
            redireccion.addFlashAttribute("exito", "Se registró la factura N.º " + factura.getNumeroFactura() + " de "
                    + factura.getRazonSocialProveedor() + ".");
            return "redirect:/facturas?tipo=proveedores";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    private String formulario(FacturaProveedorFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        model.addAttribute("formulario", formulario);
        model.addAttribute("proveedores", servicioFacturas.proveedores(token(request)));
        model.addAttribute("productos", servicioFacturas.productos(token(request)));
        model.addAttribute("error", error);
        return "facturas/proveedor";
    }

    private static ResponseEntity<byte[]> pdf(byte[] contenido, String nombre) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}

package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.RespuestaPdf;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioFacturaCliente;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Facturas del cliente autenticado (HU-30): el cliente se toma del token, nunca de la URL.
 * Una factura de otro cliente responde 404.
 */
@RestController
@RequestMapping("/api/facturas/mis-facturas")
public class ControladorMisFacturas {

    private final ServicioFacturaCliente servicioFacturaCliente;

    public ControladorMisFacturas(ServicioFacturaCliente servicioFacturaCliente) {
        this.servicioFacturaCliente = servicioFacturaCliente;
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> listar(Authentication autenticacion) throws MiException {
        return ResponseEntity.ok(servicioFacturaCliente.listarFacturasDelCliente(autenticacion.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> consultar(Authentication autenticacion, @PathVariable String id)
            throws MiException {
        return ResponseEntity.ok(servicioFacturaCliente.consultarFacturaDelCliente(autenticacion.getName(), id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(Authentication autenticacion, @PathVariable String id) throws MiException {
        FacturaResponseDTO factura = servicioFacturaCliente.consultarFacturaDelCliente(autenticacion.getName(), id);
        return RespuestaPdf.de(servicioFacturaCliente.generarPdfDelCliente(autenticacion.getName(), id),
                "Factura_DulceMayor_" + factura.numeroFactura() + ".pdf");
    }
}

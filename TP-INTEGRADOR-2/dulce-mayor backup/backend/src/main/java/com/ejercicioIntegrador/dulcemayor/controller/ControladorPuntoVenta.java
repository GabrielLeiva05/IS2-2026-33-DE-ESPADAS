package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Puntos de venta (HU-17). Endpoints heredados de BaseController (alta, consulta, listado, modificación y baja);
 * los permisos por rol están en SecurityConfig.
 */
@RestController
@RequestMapping("/api/puntos-venta")
public class ControladorPuntoVenta extends BaseController<PuntoVentaRequestDTO, PuntoVentaResponseDTO> {

    private final ServicioPuntoVenta servicioPuntoVenta;

    public ControladorPuntoVenta(ServicioPuntoVenta servicioPuntoVenta) {
        super(servicioPuntoVenta);
        this.servicioPuntoVenta = servicioPuntoVenta;
    }

    @GetMapping("/cuit/{cuit}")
    public ResponseEntity<PuntoVentaResponseDTO> buscarPorCuit(@PathVariable String cuit) throws MiException {
        return ResponseEntity.ok(servicioPuntoVenta.buscarPuntoVentaPorCuit(cuit));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PuntoVentaResponseDTO>> listarPorCliente(@PathVariable String idCliente) throws MiException {
        return ResponseEntity.ok(servicioPuntoVenta.listarPuntoVentaPorCliente(idCliente));
    }
}

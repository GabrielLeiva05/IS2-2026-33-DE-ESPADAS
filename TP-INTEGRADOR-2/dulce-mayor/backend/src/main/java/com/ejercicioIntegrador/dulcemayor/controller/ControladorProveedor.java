package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ProveedorRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProveedorResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProveedor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ABM de proveedores (ADMINISTRATIVO y JEFE). */
@RestController
@RequestMapping("/api/proveedores")
public class ControladorProveedor extends BaseController<ProveedorRequestDTO, ProveedorResponseDTO> {

    private final ServicioProveedor servicioProveedor;

    public ControladorProveedor(ServicioProveedor servicioProveedor) {
        super(servicioProveedor);
        this.servicioProveedor = servicioProveedor;
    }

    @GetMapping("/cuit/{cuit}")
    public ResponseEntity<ProveedorResponseDTO> buscarPorCuit(@PathVariable String cuit) throws MiException {
        return ResponseEntity.ok(servicioProveedor.buscarProveedorPorCuit(cuit));
    }
}

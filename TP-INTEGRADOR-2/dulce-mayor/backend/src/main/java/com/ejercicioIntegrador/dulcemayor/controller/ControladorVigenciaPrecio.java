package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vigencias-precio")
public class ControladorVigenciaPrecio extends BaseController<VigenciaPrecioRequestDTO, VigenciaPrecioResponseDTO> {

    private final ServicioVigenciaPrecio servicioVigenciaPrecio;

    public ControladorVigenciaPrecio(ServicioVigenciaPrecio servicioVigenciaPrecio) {
        super(servicioVigenciaPrecio);
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<VigenciaPrecioResponseDTO>> listarActivasPorProducto(@PathVariable String idProducto)
            throws MiException {
        return ResponseEntity.ok(servicioVigenciaPrecio.listarVigenciaPrecioActivo(idProducto));
    }

    @GetMapping("/producto/{idProducto}/todas")
    public ResponseEntity<List<VigenciaPrecioResponseDTO>> listarPorProducto(@PathVariable String idProducto)
            throws MiException {
        return ResponseEntity.ok(servicioVigenciaPrecio.listarVigenciaPrecio(idProducto));
    }

    @GetMapping("/producto/{idProducto}/vigente")
    public ResponseEntity<VigenciaPrecioResponseDTO> vigente(@PathVariable String idProducto) throws MiException {
        return ResponseEntity.ok(servicioVigenciaPrecio.buscarVigenciaPrecioVigente(idProducto));
    }
}

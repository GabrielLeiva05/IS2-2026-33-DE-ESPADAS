package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioDetalleFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-factura")
public class ControladorDetalleFactura {

    @Autowired
    private ServicioDetalleFactura servicioDetalleFactura;

    @GetMapping
    public ResponseEntity<List<DetalleFactura>> listarTodos() {
        return ResponseEntity.ok(servicioDetalleFactura.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleFactura> obtenerPorId(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(servicioDetalleFactura.buscarPorId(id));
    }

    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<List<DetalleFactura>> listarPorFactura(@PathVariable String facturaId) {
        return ResponseEntity.ok(servicioDetalleFactura.listarPorFactura(facturaId));
    }

    @PutMapping("/{id}/producto")
    public ResponseEntity<DetalleFactura> modificarProducto(
            @PathVariable String id,
            @RequestParam String codigoProducto) throws Exception {
        return ResponseEntity.ok(servicioDetalleFactura.modificarDetalleFactura(id, codigoProducto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws Exception {
        servicioDetalleFactura.eliminarDetalleFactura(id);
        return ResponseEntity.noContent().build();
    }
}
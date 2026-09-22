package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioDetalleCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-compra")
public class ControladorDetalleCompra {

    @Autowired
    private ServicioDetalleCompra servicioDetalleCompra;

    @GetMapping
    public ResponseEntity<List<DetalleCompra>> listarTodos() {
        return ResponseEntity.ok(servicioDetalleCompra.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompra> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(servicioDetalleCompra.buscarPorId(id));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<DetalleCompra>> listarPorOrden(@PathVariable String ordenId) {
        return ResponseEntity.ok(servicioDetalleCompra.listarPorOrden(ordenId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicioDetalleCompra.eliminarDetalleCompra(id);
        return ResponseEntity.noContent().build();
    }
}
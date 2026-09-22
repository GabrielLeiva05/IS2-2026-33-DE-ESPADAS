package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
public class ControladorOrdenCompra {

    @Autowired
    private ServicioOrdenCompra servicioOrdenCompra;

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> listarActivas() {
        return ResponseEntity.ok(servicioOrdenCompra.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(servicioOrdenCompra.buscarPorId(id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<OrdenCompra>> listarPorEstado(@PathVariable EstadoOrdenCompra estado) {
        return ResponseEntity.ok(servicioOrdenCompra.listarPorEstado(estado));
    }

    @PostMapping
    public ResponseEntity<OrdenCompra> crear(@RequestParam String identificadorCompra) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioOrdenCompra.crearOrdenCompra(identificadorCompra));
    }

    @PostMapping("/{ordenId}/detalles")
    public ResponseEntity<OrdenCompra> agregarDetalle(
            @PathVariable String ordenId,
            @RequestParam String productoId,
            @RequestParam int cantidad,
            @RequestParam double precioUnitario) {
        return ResponseEntity.ok(servicioOrdenCompra.agregarDetalleAOrden(ordenId, productoId, cantidad, precioUnitario));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenCompra> cambiarEstado(
            @PathVariable String id,
            @RequestParam EstadoOrdenCompra estado) {
        return ResponseEntity.ok(servicioOrdenCompra.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicioOrdenCompra.eliminarOrdenCompra(id);
        return ResponseEntity.noContent().build();
    }
}
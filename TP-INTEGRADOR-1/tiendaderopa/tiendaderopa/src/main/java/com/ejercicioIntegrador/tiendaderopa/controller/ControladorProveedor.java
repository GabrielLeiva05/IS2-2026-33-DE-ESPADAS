package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioRegistroProveedor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/proveedores")
public class ControladorProveedor {

    private final ServicioProveedor svcProveedor;
    private final ServicioRegistroProveedor svcRegistroProveedor;

    public ControladorProveedor(ServicioProveedor svcProveedor, ServicioRegistroProveedor svcRegistroProveedor) {
        this.svcProveedor = svcProveedor;
        this.svcRegistroProveedor = svcRegistroProveedor;
    }

    @GetMapping
    public Collection<Proveedor> listar() { return svcProveedor.listarProveedor(); }

    @GetMapping("/activos")
    public Collection<Proveedor> listarActivos() { return svcProveedor.listarProveedorActivo(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        try { return ResponseEntity.ok(svcProveedor.buscarProveedor(id)); }
        catch (MiException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestParam String razonSocial,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String telefonoFijo,
                                    @RequestParam(required = false) String telefonoCelular) {
        try {
            Proveedor proveedor = svcRegistroProveedor.registrarProveedor(razonSocial, email, telefonoFijo, telefonoCelular);
            return ResponseEntity.status(HttpStatus.CREATED).body(proveedor);
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable String id, @RequestParam String razonSocial) {
        try { return ResponseEntity.ok(svcProveedor.modificarProveedor(id, razonSocial)); }
        catch (MiException e) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try { svcProveedor.eliminarProveedor(id); return ResponseEntity.noContent().build(); }
        catch (MiException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}
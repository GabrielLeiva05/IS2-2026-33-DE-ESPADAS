package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
public class ControladorOrdenCompra {

    @Autowired
    private ServicioOrdenCompra servicioOrdenCompra;

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> listarActivas(Authentication authentication) throws Exception {
        if (esAdministrativo(authentication)) {
            return ResponseEntity.ok(servicioOrdenCompra.listarActivas());
        }
        return ResponseEntity.ok(servicioOrdenCompra.listarActivasDeUsuario(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> obtenerPorId(@PathVariable String id, Authentication authentication) throws Exception {
        return ResponseEntity.ok(servicioOrdenCompra.buscarAccesibleParaUsuario(
                id, authentication.getName(), esAdministrativo(authentication)));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<OrdenCompra>> listarPorEstado(@PathVariable EstadoOrdenCompra estado,
                                                              Authentication authentication) throws Exception {
        if (esAdministrativo(authentication)) {
            return ResponseEntity.ok(servicioOrdenCompra.listarPorEstado(estado));
        }
        return ResponseEntity.ok(servicioOrdenCompra.listarActivasDeUsuario(authentication.getName()).stream()
                .filter(orden -> orden.getEstadoOrdenCompra() == estado)
                .toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrdenCompra> crear(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails usuario)
            throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioOrdenCompra.obtenerCarrito(usuario.getUsername()));
    }

    @PostMapping("/{ordenId}/detalles")
    public ResponseEntity<OrdenCompra> agregarDetalle(
            @PathVariable String ordenId,
            @RequestParam String productoId,
            @RequestParam int cantidad,
            Authentication authentication) throws Exception {
            return ResponseEntity.ok(servicioOrdenCompra.agregarDetalleAOrden(ordenId, productoId, cantidad,
                authentication.getName(), esAdministrativo(authentication)));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<OrdenCompra> cambiarEstado(
            @PathVariable String id,
            @RequestParam EstadoOrdenCompra estado) {
        return ResponseEntity.ok(servicioOrdenCompra.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable String id, Authentication authentication) throws Exception {
        servicioOrdenCompra.anularOrdenDeUsuario(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrativo(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRATIVO"));
    }
}
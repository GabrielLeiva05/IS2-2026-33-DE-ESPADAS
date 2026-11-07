package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.DescuentoPromocionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PrevisualizarPromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Administración de códigos promocionales (personal administrativo; el jefe lo hereda). */
@RestController
@RequestMapping("/api/promociones")
public class ControladorPromocion {

    private final ServicioPromocion servicioPromocion;

    public ControladorPromocion(ServicioPromocion servicioPromocion) {
        this.servicioPromocion = servicioPromocion;
    }

    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> listar() {
        return ResponseEntity.ok(servicioPromocion.listar());
    }

    @PostMapping
    public ResponseEntity<PromocionResponseDTO> alta(@RequestBody PromocionRequestDTO dto) throws MiException {
        PromocionResponseDTO creada = servicioPromocion.alta(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> modificar(@PathVariable String id,
                                                          @RequestBody PromocionRequestDTO dto) throws MiException {
        return ResponseEntity.ok(servicioPromocion.modificar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable String id) throws MiException {
        servicioPromocion.baja(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/previsualizar")
    public ResponseEntity<DescuentoPromocionDTO> previsualizar(
            @RequestBody PrevisualizarPromocionRequestDTO dto) throws MiException {
        if (dto == null) {
            throw new MiException("Indicá el código y el subtotal");
        }
        return ResponseEntity.ok(servicioPromocion.previsualizar(dto.codigo(), dto.subtotal()));
    }
}

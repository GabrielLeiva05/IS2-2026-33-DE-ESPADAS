package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ControladorProducto extends BaseController<ProductoRequestDTO, ProductoResponseDTO> {

    private final ServicioProducto servicioProducto;

    public ControladorProducto(ServicioProducto servicioProducto) {
        super(servicioProducto);
        this.servicioProducto = servicioProducto;
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> buscarPorCodigo(@PathVariable String codigo) throws MiException {
        return ResponseEntity.ok(servicioProducto.buscarProductoPorCodigo(codigo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ProductoResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioProducto.buscarProductoPorNombre(nombre));
    }

    @GetMapping("/ofertas")
    public ResponseEntity<List<ProductoResponseDTO>> listarEnOferta() {
        return ResponseEntity.ok(servicioProducto.listarProductoEnOferta());
    }
}

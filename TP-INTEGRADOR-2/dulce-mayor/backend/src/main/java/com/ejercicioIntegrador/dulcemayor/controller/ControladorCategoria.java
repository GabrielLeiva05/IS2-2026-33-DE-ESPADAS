package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCategoria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class ControladorCategoria extends BaseController<CategoriaRequestDTO, CategoriaResponseDTO> {

    private final ServicioCategoria servicioCategoria;

    public ControladorCategoria(ServicioCategoria servicioCategoria) {
        super(servicioCategoria);
        this.servicioCategoria = servicioCategoria;
    }

    @GetMapping("/buscar")
    public ResponseEntity<CategoriaResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioCategoria.buscarCategoriaPorNombre(nombre));
    }
}

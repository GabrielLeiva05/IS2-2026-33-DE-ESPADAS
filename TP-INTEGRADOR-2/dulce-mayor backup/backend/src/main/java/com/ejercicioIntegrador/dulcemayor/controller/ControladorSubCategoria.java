package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSubCategoria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subcategorias")
public class ControladorSubCategoria extends BaseController<SubCategoriaRequestDTO, SubCategoriaResponseDTO> {

    private final ServicioSubCategoria servicioSubCategoria;

    public ControladorSubCategoria(ServicioSubCategoria servicioSubCategoria) {
        super(servicioSubCategoria);
        this.servicioSubCategoria = servicioSubCategoria;
    }

    @GetMapping("/buscar")
    public ResponseEntity<SubCategoriaResponseDTO> buscarPorNombre(@RequestParam String nombre) throws MiException {
        return ResponseEntity.ok(servicioSubCategoria.buscarSubCategoriaPorNombre(nombre));
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<SubCategoriaResponseDTO>> listarActivasPorCategoria(@PathVariable String idCategoria)
            throws MiException {
        return ResponseEntity.ok(servicioSubCategoria.listarSubCategoriaActivo(idCategoria));
    }

    @GetMapping("/categoria/{idCategoria}/todas")
    public ResponseEntity<List<SubCategoriaResponseDTO>> listarPorCategoria(@PathVariable String idCategoria)
            throws MiException {
        return ResponseEntity.ok(servicioSubCategoria.listarSubCategoria(idCategoria));
    }
}

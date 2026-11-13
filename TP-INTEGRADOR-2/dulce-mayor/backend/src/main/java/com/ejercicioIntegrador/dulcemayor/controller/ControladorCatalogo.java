package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.FiltroCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCatalogo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo público (sitio institucional) y catálogo del preventista.
 * Es de solo lectura y no administra una entidad, por eso no hereda de BaseController.
 */
@RestController
@RequestMapping("/api/catalogo")
public class ControladorCatalogo {

    private final ServicioCatalogo servicioCatalogo;

    public ControladorCatalogo(ServicioCatalogo servicioCatalogo) {
        this.servicioCatalogo = servicioCatalogo;
    }

    @GetMapping
    public ResponseEntity<List<ProductoCatalogoDTO>> listar(@RequestParam(required = false) String idCategoria,
                                                            @RequestParam(required = false) String idSubCategoria,
                                                            @RequestParam(required = false) String texto,
                                                            @RequestParam(defaultValue = "false") boolean soloOfertas) {
        FiltroCatalogoDTO filtro = new FiltroCatalogoDTO(idCategoria, idSubCategoria, texto, soloOfertas);
        return ResponseEntity.ok(servicioCatalogo.listarProductoConStock(filtro));
    }

    @GetMapping("/{idProducto}")
    public ResponseEntity<ProductoCatalogoDTO> consultar(@PathVariable String idProducto) throws MiException {
        return ResponseEntity.ok(servicioCatalogo.consultarProductoConPrecios(idProducto));
    }
}

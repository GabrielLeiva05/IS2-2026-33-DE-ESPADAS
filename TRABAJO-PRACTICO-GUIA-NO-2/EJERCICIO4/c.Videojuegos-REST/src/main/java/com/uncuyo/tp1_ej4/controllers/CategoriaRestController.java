package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.services.ServicioCategoria;
import org.springframework.web.bind.annotation.*;

/** API REST de categorías; comparte el servicio y la persistencia con la interfaz web. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController extends RestControllerBase<Categoria> {
    public CategoriaRestController(ServicioCategoria servicio) {
        super(servicio);
    }
}

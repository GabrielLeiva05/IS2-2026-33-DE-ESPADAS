package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.services.ServicioVideojuego;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints JSON para videojuegos. Las páginas Thymeleaf existentes mantienen
 * sus rutas; los clientes REST consumen esta API bajo /api/videojuegos.
 */
@RestController
@RequestMapping("/api/videojuegos")
public class VideojuegoRestController extends RestControllerBase<Videojuego> {
    private final ServicioVideojuego servicio;

    public VideojuegoRestController(ServicioVideojuego servicio) {
        super(servicio);
        this.servicio = servicio;
    }

    @Override
    protected List<Videojuego> listarRecursos(String query) {
        // Sin filtro se consulta el inventario completo; con query se reutiliza la búsqueda de títulos activos.
        return query == null || query.isBlank()
                ? servicio.findAll()
                : servicio.findByTitle(query);
    }
}

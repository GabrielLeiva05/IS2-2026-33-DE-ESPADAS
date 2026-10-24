package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.services.ServicioEstudio;
import org.springframework.web.bind.annotation.*;

/** API REST de estudios; comparte el servicio y la persistencia con la interfaz web. */
@RestController
@RequestMapping("/api/estudios")
public class EstudioRestController extends RestControllerBase<Estudio> {
    public EstudioRestController(ServicioEstudio servicio) {
        super(servicio);
    }
}

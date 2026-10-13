package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.services.LocalidadService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/localidades")
public class LocalidadController extends ControladorBase<Localidad, LocalidadService> {
    public LocalidadController(LocalidadService service) {
        super(service);
    }
}
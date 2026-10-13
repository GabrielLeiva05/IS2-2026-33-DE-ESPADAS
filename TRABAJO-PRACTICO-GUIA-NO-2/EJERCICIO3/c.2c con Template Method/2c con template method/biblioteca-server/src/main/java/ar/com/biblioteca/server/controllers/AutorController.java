package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.services.AutorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autores")
public class AutorController extends ControladorBase<Autor, AutorService> {
    public AutorController(AutorService service) {
        super(service);
    }
}
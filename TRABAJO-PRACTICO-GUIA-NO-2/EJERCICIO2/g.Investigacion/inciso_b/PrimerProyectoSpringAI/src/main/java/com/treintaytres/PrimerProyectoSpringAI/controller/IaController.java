package com.treintaytres.PrimerProyectoSpringAI.controller;

import com.treintaytres.PrimerProyectoSpringAI.service.IaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ia")
public class IaController {

    private final IaService iaService;

    public IaController(IaService iaService) {
        this.iaService = iaService;
    }

    @GetMapping("/preguntar")
    public ResponseEntity<String> preguntar(@RequestParam String pregunta){
        String respuesta = iaService.preguntar(pregunta);
        return ResponseEntity.ok(respuesta);
    }
}

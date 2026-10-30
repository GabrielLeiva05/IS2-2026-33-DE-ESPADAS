package com.competencia.blockazo.controller;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.AuthResponse;
import com.competencia.blockazo.model.dto.LoginRequest;
import com.competencia.blockazo.model.dto.RegistroRequest;
import com.competencia.blockazo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints publicos de autenticacion. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Alta de una persona (register). Devuelve el JWT para quedar logueado. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) throws ErrorServiceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /** Login con correo (usuario) y clave. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws ErrorServiceException {
        return ResponseEntity.ok(authService.login(request));
    }

    /** URL a la que el frontend debe redirigir el navegador para ingresar con Google. */
    @GetMapping("/google")
    public Map<String, String> urlGoogle() {
        return Map.of("url", "/oauth2/authorization/google");
    }
}

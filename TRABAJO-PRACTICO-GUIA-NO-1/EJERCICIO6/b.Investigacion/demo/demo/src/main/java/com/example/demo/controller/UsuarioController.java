package com.example.demo.controller;

import com.example.demo.domain.Usuario;
import com.example.demo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public Usuario registrar(@RequestParam String username, @RequestParam String password) {
        return usuarioService.registrarUsuario(username, password);
    }
}
package com.example.demo.service;

import com.example.demo.domain.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(String username, String passwordPlano) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(passwordPlano)); // acá se encripta
        return usuarioRepository.save(usuario);
    }
    
    public boolean verificarPassword(String passwordPlano, String passwordEncriptada) {
        return passwordEncoder.matches(passwordPlano, passwordEncriptada);
    }
    
}
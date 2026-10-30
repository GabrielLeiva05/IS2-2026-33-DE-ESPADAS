package com.competencia.blockazo.service;

import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Rol;
import com.competencia.blockazo.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puente entre nuestro Usuario y Spring Security.
 * El CORREO es el username. Un usuario dado de baja queda "disabled" y no puede loguearse.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese correo"));

        Rol rol = usuario.getRol() != null ? usuario.getRol() : Rol.USUARIO;

        return User.withUsername(usuario.getCorreo())
                // Los usuarios de Google no tienen clave: con "" nunca pueden entrar por formulario
                .password(usuario.getClave() != null ? usuario.getClave() : "")
                .roles(rol.name())                              // -> ROLE_USUARIO / ROLE_ADMIN
                .disabled(Boolean.TRUE.equals(usuario.isEliminado()))
                .build();
    }
}

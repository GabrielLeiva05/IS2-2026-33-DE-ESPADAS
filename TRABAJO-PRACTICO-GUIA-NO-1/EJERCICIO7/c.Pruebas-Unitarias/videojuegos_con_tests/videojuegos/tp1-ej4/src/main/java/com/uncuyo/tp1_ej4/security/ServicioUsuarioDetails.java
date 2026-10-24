package com.uncuyo.tp1_ej4.security;

import com.uncuyo.tp1_ej4.entities.Usuario;
import com.uncuyo.tp1_ej4.repositories.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre nuestra entidad Usuario y Spring Security: le dice al
 * framework como buscar un usuario y que roles tiene, para poder validar
 * el login y despues autorizar (hasRole) segun corresponda.
 */
@Service
public class ServicioUsuarioDetails implements UserDetailsService {

    @Autowired
    private RepositorioUsuario repoUsuario;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repoUsuario.findByUsernameAndActivo(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String[] roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .toArray(String[]::new);

        if (roles.length == 0) {
            // Si por algun motivo el usuario no tiene rol asignado, se lo
            // trata como USER comun (nunca como ADMIN por defecto).
            roles = new String[]{"USER"};
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // ya viene encriptado (BCrypt)
                .roles(roles)
                .build();
    }
}

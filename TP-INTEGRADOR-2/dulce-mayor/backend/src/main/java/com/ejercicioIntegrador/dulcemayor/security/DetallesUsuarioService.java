package com.ejercicioIntegrador.dulcemayor.security;

import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Puente entre Usuario y Spring Security (adaptado de UsuarioDetailsService de Blockazo).
 * No accede al repositorio: consulta a ServicioUsuario, respetando la separación de capas.
 *  - Solo se encuentran cuentas activas: una cuenta dada de baja no puede autenticarse.
 *  - Con debeCambiarClave = true la única autoridad es CAMBIO_CLAVE_PENDIENTE (HU-06).
 *  - Las cuentas de Google/Facebook no tienen clave: nunca pueden ingresar por formulario.
 */
@Component
public class DetallesUsuarioService implements UserDetailsService {

    private final ServicioUsuario servicioUsuario;

    public DetallesUsuarioService(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta activa con ese correo"));
        User.UserBuilder builder = User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getClave() == null ? "" : usuario.getClave());
        if (usuario.isDebeCambiarClave()) {
            builder.authorities(AutoridadesSeguridad.CAMBIO_CLAVE_PENDIENTE);
        } else {
            builder.roles(usuario.getRol().name());
        }
        return builder.build();
    }
}

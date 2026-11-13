package com.ejercicioIntegrador.dulcemayor.security;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetallesUsuarioServiceTest {

    private ServicioUsuario servicioUsuario;
    private DetallesUsuarioService servicio;

    @BeforeEach
    void setUp() {
        servicioUsuario = mock(ServicioUsuario.class);
        servicio = new DetallesUsuarioService(servicioUsuario);
    }

    private Usuario cuenta(RolUsuario rol, boolean debeCambiarClave, String clave) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("ana@dulcemayor.com");
        usuario.setRol(rol);
        usuario.setDebeCambiarClave(debeCambiarClave);
        usuario.setClave(clave);
        usuario.setProveedorAutenticacion(ProveedorAutenticacion.LOCAL);
        when(servicioUsuario.buscarActivoPorNombreUsuario("ana@dulcemayor.com")).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private static List<String> autoridades(UserDetails detalles) {
        return detalles.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void conLaClaveTemporalSoloTieneLaAutoridadDeCambiarla() {
        cuenta(RolUsuario.JEFE, true, "hash");

        assertEquals(List.of(AutoridadesSeguridad.CAMBIO_CLAVE_PENDIENTE),
                autoridades(servicio.loadUserByUsername("ana@dulcemayor.com")));
    }

    @Test
    void sinCambioPendienteRecibeSuRol() {
        cuenta(RolUsuario.PREVENTISTA, false, "hash");

        assertEquals(List.of("ROLE_PREVENTISTA"), autoridades(servicio.loadUserByUsername("ana@dulcemayor.com")));
    }

    @Test
    void unaCuentaDeRedSocialNoTieneClaveUtilizable() {
        cuenta(RolUsuario.CLIENTE, false, null);

        assertEquals("", servicio.loadUserByUsername("ana@dulcemayor.com").getPassword());
    }

    @Test
    void unaCuentaInexistenteODadaDeBajaNoSeEncuentra() {
        when(servicioUsuario.buscarActivoPorNombreUsuario("nadie@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("nadie@mail.com"));
    }
}

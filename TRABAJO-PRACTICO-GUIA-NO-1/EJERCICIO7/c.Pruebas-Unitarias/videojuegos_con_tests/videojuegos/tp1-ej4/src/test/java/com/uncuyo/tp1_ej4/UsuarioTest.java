package com.uncuyo.tp1_ej4;

import com.uncuyo.tp1_ej4.entities.Rol;
import com.uncuyo.tp1_ej4.entities.Usuario;
import com.uncuyo.tp1_ej4.repositories.RepositorioUsuario;
import com.uncuyo.tp1_ej4.security.ServicioUsuarioDetails;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioTest {

    @Mock
    private RepositorioUsuario repoUsuario;

    @InjectMocks
    private ServicioUsuarioDetails servicioUsuarioDetails;

    private Usuario usuario;

    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAll UsuarioTest");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll UsuarioTest");
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        System.out.println("beforeEach " + testInfo.getDisplayName());

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("martina");
        usuario.setPassword("$2a$10$hashBCryptDeEjemplo"); // ya viene encriptado
        usuario.setActivo(true);
        usuario.setRoles(new HashSet<>());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        System.out.println("afterEach " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    public void testLoadUserByUsernameConRolesMapeaCorrectamente() {
        Rol admin = new Rol();
        admin.setId(1L);
        admin.setNombre("ADMIN");
        Rol user = new Rol();
        user.setId(2L);
        user.setNombre("USER");

        Set<Rol> roles = new HashSet<>();
        roles.add(admin);
        roles.add(user);
        usuario.setRoles(roles);

        when(repoUsuario.findByUsernameAndActivo("martina", true)).thenReturn(Optional.of(usuario));

        UserDetails resultado = servicioUsuarioDetails.loadUserByUsername("martina");

        Assertions.assertEquals("martina", resultado.getUsername());
        Assertions.assertEquals("$2a$10$hashBCryptDeEjemplo", resultado.getPassword());
        Set<String> authorities = resultado.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        Assertions.assertTrue(authorities.contains("ROLE_ADMIN"));
        Assertions.assertTrue(authorities.contains("ROLE_USER"));
        Assertions.assertEquals(2, authorities.size());

        System.out.println("resultado authorities=" + authorities);
    }

    @Test
    @Order(2)
    public void testLoadUserByUsernameInexistenteLanzaUsernameNotFoundException() {
        when(repoUsuario.findByUsernameAndActivo("fantasma", true)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            servicioUsuarioDetails.loadUserByUsername("fantasma");
        });

        Assertions.assertEquals("Usuario no encontrado: fantasma", ex.getMessage());

        System.out.println("resultado " + ex.getMessage());
    }

    @Test
    @Order(3)
    public void testLoadUserByUsernameSinRolesAsignaUserPorDefecto() {
        usuario.setRoles(new HashSet<>()); // sin roles asignados

        when(repoUsuario.findByUsernameAndActivo("martina", true)).thenReturn(Optional.of(usuario));

        UserDetails resultado = servicioUsuarioDetails.loadUserByUsername("martina");

        Set<String> authorities = resultado.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        Assertions.assertEquals(1, authorities.size());
        Assertions.assertTrue(authorities.contains("ROLE_USER"));
        Assertions.assertFalse(authorities.contains("ROLE_ADMIN"),
                "Un usuario sin roles nunca debe quedar como ADMIN por defecto");

        System.out.println("resultado authorities=" + authorities);
    }

    @Test
    @Order(4)
    public void testLoadUserByUsernameSoloBuscaUsuariosActivos() {
        when(repoUsuario.findByUsernameAndActivo("martina", true)).thenReturn(Optional.of(usuario));

        servicioUsuarioDetails.loadUserByUsername("martina");

        // Verifica que siempre se filtra por activo=true, para que un usuario
        // deshabilitado no pueda loguearse aunque exista en la base.
        verify(repoUsuario, times(1)).findByUsernameAndActivo("martina", true);
    }
}

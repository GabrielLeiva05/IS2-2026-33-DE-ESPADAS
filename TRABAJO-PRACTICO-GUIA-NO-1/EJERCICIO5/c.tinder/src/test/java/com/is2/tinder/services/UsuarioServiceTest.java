package com.is2.tinder.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.is2.tinder.entities.Usuario;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.repositories.UsuarioRepository;
import com.is2.tinder.repositories.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FotoService fotoService;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private ZonaRepository zonaRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, fotoService, notificacionService, zonaRepository);
    }

    @Test
    void validarRechazaNombreVacio() {
        ErrorService error = assertThrows(ErrorService.class,
                () -> usuarioService.validar("", "Perez", "ana@example.com", "secreto"));

        assertEquals("El nombre del usuario no puede ser nulo", error.getMessage());
    }

    @Test
    void loginNormalizaCorreoYDevuelveUsuarioActivo() throws ErrorService {
        Usuario usuario = new Usuario();
        usuario.setId("usuario-1");
        usuario.setMail("ana@example.com");
        usuario.setClave(new BCryptPasswordEncoder().encode("secreto"));
        when(usuarioRepository.buscarPorMail("ana@example.com")).thenReturn(usuario);

        var resultado = usuarioService.login("  ANA@EXAMPLE.COM ", "secreto");

        assertNotNull(resultado);
        assertEquals("usuario-1", resultado.getId());
        verify(usuarioRepository).buscarPorMail("ana@example.com");
    }

    @Test
    void loginRechazaUsuarioDadoDeBaja() {
        Usuario usuario = new Usuario();
        usuario.setMail("ana@example.com");
        usuario.setClave(new BCryptPasswordEncoder().encode("secreto"));
        usuario.setBaja(java.time.LocalDateTime.now());
        when(usuarioRepository.buscarPorMail("ana@example.com")).thenReturn(usuario);

        ErrorService error = assertThrows(ErrorService.class,
                () -> usuarioService.login("ana@example.com", "secreto"));

        assertEquals("No existe usuario con ese correo y clave", error.getMessage());
    }
}
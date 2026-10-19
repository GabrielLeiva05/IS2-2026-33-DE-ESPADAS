package com.tienda.carrito.business;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.UsuarioService;
import com.tienda.carrito.business.percistence.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de usuarios y roles. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repositorio;
    @Mock
    PasswordEncoder encoder;

    UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(repositorio, encoder);
    }

    private Usuario usuario(String id, String nombre, String clave, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        u.setClave(clave);
        u.setRol(rol);
        return u;
    }

    @Test
    void registrarCliente_siempreCreaCliente_auncuandoLleguePedidoDeAdmin() throws Exception {
        when(encoder.encode("secreto1")).thenReturn("HASH");
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = servicio.registrarCliente(usuario(null, "ana", "secreto1", Rol.ADMIN));

        assertEquals(Rol.CLIENTE, creado.getRol());
        assertEquals("HASH", creado.getClave());
    }

    @Test
    void alta_sinRol_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(usuario(null, "ana", "secreto1", null)));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void alta_conNombreDuplicado_lanzaError() {
        when(repositorio.buscarUsuarioPorNombre("ana")).thenReturn(usuario("x", "ana", "H", Rol.CLIENTE));

        assertThrows(ErrorServiceException.class, () -> servicio.alta(usuario(null, "ana", "secreto1", Rol.CLIENTE)));
    }

    @Test
    void alta_conClaveCorta_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(usuario(null, "ana", "123", Rol.CLIENTE)));
    }

    @Test
    void baja_delUnicoAdministrador_lanzaError() {
        when(repositorio.findById("1")).thenReturn(Optional.of(usuario("1", "admin", "H", Rol.ADMIN)));
        when(repositorio.contarUsuariosActivosPorRol(Rol.ADMIN)).thenReturn(1L);

        assertThrows(ErrorServiceException.class, () -> servicio.baja("1"));

        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void baja_deUnAdministradorHabiendoOtro_esPermitida() throws Exception {
        Usuario admin = usuario("1", "admin2", "H", Rol.ADMIN);
        when(repositorio.findById("1")).thenReturn(Optional.of(admin));
        when(repositorio.contarUsuariosActivosPorRol(Rol.ADMIN)).thenReturn(2L);

        servicio.baja("1");

        assertTrue(admin.isEliminado());
    }

    @Test
    void baja_deLaPropiaCuenta_lanzaError_sinTocarLaBase() {
        assertThrows(ErrorServiceException.class, () -> servicio.baja("1", "1"));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void baja_deOtroUsuario_conActorDistinto_seRealiza() throws Exception {
        Usuario cliente = usuario("2", "ana", "H", Rol.CLIENTE);
        when(repositorio.findById("2")).thenReturn(Optional.of(cliente));

        servicio.baja("2", "1");

        assertTrue(cliente.isEliminado());
    }

    @Test
    void modificar_quitarElRolAlUnicoAdmin_lanzaError() {
        when(repositorio.findById("1")).thenReturn(Optional.of(usuario("1", "admin", "H", Rol.ADMIN)));
        when(repositorio.contarUsuariosActivosPorRol(Rol.ADMIN)).thenReturn(1L);

        assertThrows(ErrorServiceException.class,
                () -> servicio.modificar("1", usuario(null, "admin", "", Rol.CLIENTE)));

        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void modificar_sinClaveNueva_conservaElHashActual() throws Exception {
        Usuario existente = usuario("1", "ana", "HASH_VIEJO", Rol.CLIENTE);
        when(repositorio.findById("1")).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Usuario> resultado = servicio.modificar("1", usuario(null, "ana", "", Rol.CLIENTE));

        assertTrue(resultado.isPresent());
        assertEquals("HASH_VIEJO", resultado.get().getClave());
    }

    @Test
    void autenticar_conClaveCorrecta_devuelveElUsuario() throws Exception {
        Usuario ana = usuario("1", "ana", "HASH", Rol.CLIENTE);
        when(repositorio.buscarUsuarioPorNombre("ana")).thenReturn(ana);
        when(encoder.matches("secreto1", "HASH")).thenReturn(true);

        assertEquals(ana, servicio.autenticar("ana", "secreto1"));
    }

    @Test
    void autenticar_conClaveIncorrecta_lanzaError() {
        when(repositorio.buscarUsuarioPorNombre("ana")).thenReturn(usuario("1", "ana", "HASH", Rol.CLIENTE));
        when(encoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(ErrorServiceException.class, () -> servicio.autenticar("ana", "mala"));
    }
}

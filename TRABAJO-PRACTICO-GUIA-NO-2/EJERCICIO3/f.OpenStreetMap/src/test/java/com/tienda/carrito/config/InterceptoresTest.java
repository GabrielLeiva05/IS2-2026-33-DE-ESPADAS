package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/** Control de acceso por roles: lo que pedia el usuario (un CLIENTE no puede entrar a la administracion). */
@ExtendWith(MockitoExtension.class)
class InterceptoresTest {

    @Mock
    UsuarioService usuarioService;

    private Usuario usuario(Rol rol) {
        Usuario u = new Usuario();
        u.setId("1");
        u.setNombre("x");
        u.setRol(rol);
        return u;
    }

    // ---------- AutorizacionInterceptor ----------

    @Test
    void rutaAdmin_sinSesion_redirigeAlLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pasa = new AutorizacionInterceptor(Rol.ADMIN).preHandle(request, response, new Object());

        assertFalse(pasa);
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    void rutaAdmin_conCliente_devuelve403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SesionUsuario.ACTUAL, usuario(Rol.CLIENTE));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pasa = new AutorizacionInterceptor(Rol.ADMIN).preHandle(request, response, new Object());

        assertFalse(pasa);
        assertEquals(403, response.getStatus());
    }

    @Test
    void rutaAdmin_conAdmin_pasa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SesionUsuario.ACTUAL, usuario(Rol.ADMIN));

        boolean pasa = new AutorizacionInterceptor(Rol.ADMIN).preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(pasa);
    }

    @Test
    void rutaDeUsuarioLogueado_conCliente_pasa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SesionUsuario.ACTUAL, usuario(Rol.CLIENTE));

        boolean pasa = new AutorizacionInterceptor(null).preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(pasa);
    }

    // ---------- SesionInterceptor ----------

    @Test
    void sesionDeUsuarioEliminado_seInvalida_yYaNoHayUsuarioActual() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUsuario.ID, "1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(sesion);
        when(usuarioService.obtener("1")).thenReturn(Optional.empty()); // fue dado de baja

        new SesionInterceptor(usuarioService).preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(sesion.isInvalid());
        assertNull(request.getAttribute(SesionUsuario.ACTUAL));
    }

    @Test
    void sesionDeUsuarioVigente_cargaElUsuarioActualConSuRolDeLaBase() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUsuario.ID, "1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(sesion);
        when(usuarioService.obtener("1")).thenReturn(Optional.of(usuario(Rol.CLIENTE)));

        new SesionInterceptor(usuarioService).preHandle(request, new MockHttpServletResponse(), new Object());

        assertFalse(sesion.isInvalid());
        Usuario actual = (Usuario) request.getAttribute(SesionUsuario.ACTUAL);
        assertNotNull(actual);
        assertEquals(Rol.CLIENTE, actual.getRol());
    }
}

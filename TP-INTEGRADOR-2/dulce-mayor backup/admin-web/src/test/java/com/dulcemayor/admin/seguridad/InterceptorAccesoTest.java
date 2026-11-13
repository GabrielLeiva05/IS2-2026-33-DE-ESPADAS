package com.dulcemayor.admin.seguridad;

import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.dto.UsuarioSesionApiDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Matriz de navegación de admin-web: sesión, clave temporal pendiente (HU-06) y secciones por rol. */
class InterceptorAccesoTest {

    private final InterceptorAcceso interceptor = new InterceptorAcceso(new GestorSesion());

    private static SesionUsuario sesion(String rol, boolean clavePendiente) {
        UsuarioSesionApiDTO usuario = new UsuarioSesionApiDTO();
        usuario.setRol(rol);
        usuario.setCorreo("empleado@dulcemayor.com.ar");
        usuario.setDebeCambiarClave(clavePendiente);
        SesionApiDTO respuesta = new SesionApiDTO();
        respuesta.setToken("token");
        respuesta.setUsuario(usuario);
        return SesionUsuario.desde(respuesta);
    }

    private MockHttpServletResponse pedir(SesionUsuario usuario, String ruta) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ruta);
        if (usuario != null) {
            MockHttpSession sesion = new MockHttpSession();
            sesion.setAttribute(GestorSesion.ATRIBUTO, usuario);
            request.setSession(sesion);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean continua = interceptor.preHandle(request, response, new Object());
        if (continua) {
            response.setStatus(200);
        }
        return response;
    }

    @Test
    void sinSesionSeVaAlLogin() throws Exception {
        assertEquals("/login", pedir(null, "/empleados").getRedirectedUrl());
        assertEquals(401, pedir(null, "/ubicacion/provincias/1").getStatus());
    }

    @Test
    void conLaClaveTemporalSoloSePuedeCambiarLaClaveOSalir() throws Exception {
        SesionUsuario pendiente = sesion("JEFE", true);
        assertEquals("/cambiar-clave", pedir(pendiente, "/").getRedirectedUrl());
        assertEquals("/cambiar-clave", pedir(pendiente, "/empleados").getRedirectedUrl());
        assertEquals(200, pedir(pendiente, "/cambiar-clave").getStatus());
        assertEquals(200, pedir(pendiente, "/salir").getStatus());
    }

    @Test
    void cadaSeccionExigeSuRol() throws Exception {
        assertEquals(200, pedir(sesion("JEFE", false), "/empleados/nuevo").getStatus());
        assertEquals(403, pedir(sesion("ADMINISTRATIVO", false), "/empleados").getStatus());
        assertEquals(200, pedir(sesion("ADMINISTRATIVO", false), "/clientes").getStatus());
        assertEquals(200, pedir(sesion("PREVENTISTA", false), "/catalogo").getStatus());
        assertEquals(403, pedir(sesion("PREVENTISTA", false), "/clientes").getStatus());
        assertEquals(403, pedir(sesion("DISTRIBUIDOR", false), "/catalogo").getStatus());
    }

    @Test
    void losReportesSonSoloDelJefeAdministrativo() throws Exception {
        assertEquals(200, pedir(sesion("JEFE", false), "/reportes/clientes-pedidos").getStatus());
        assertEquals(200, pedir(sesion("JEFE", false), "/reportes/ventas/excel").getStatus());
        assertEquals(403, pedir(sesion("ADMINISTRATIVO", false), "/reportes/clientes-pedidos").getStatus());
        assertEquals(403, pedir(sesion("PREVENTISTA", false), "/reportes/ventas").getStatus());
        assertEquals("/login", pedir(null, "/reportes/ventas").getRedirectedUrl());
    }

    @Test
    void elJefeHeredaLosPermisosDelAdministrativo() {
        SesionUsuario jefe = sesion("JEFE", false);
        assertTrue(jefe.isAdministrativo());
        assertTrue(jefe.isPuedeVerCatalogo());
        assertFalse(sesion("DISTRIBUIDOR", false).isPuedeVerCatalogo());
    }
}

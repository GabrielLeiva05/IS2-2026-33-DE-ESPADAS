package com.dulcemayor.institucional.seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La vuelta después del ingreso solo acepta páginas del propio sitio (evita redirecciones abiertas). */
class GestorSesionTest {

    @Test
    void aceptaRutasInternas() {
        assertTrue(GestorSesion.esDestinoInterno("/mis-pedidos/abc-123"));
        assertTrue(GestorSesion.esDestinoInterno("/mis-pedidos"));
    }

    @Test
    void rechazaDestinosExternosOMalformados() {
        assertFalse(GestorSesion.esDestinoInterno(null));
        assertFalse(GestorSesion.esDestinoInterno("https://otro.com"));
        assertFalse(GestorSesion.esDestinoInterno("//otro.com/x"));
        assertFalse(GestorSesion.esDestinoInterno("/\\otro.com"));
        assertFalse(GestorSesion.esDestinoInterno("mis-pedidos"));
        assertFalse(GestorSesion.esDestinoInterno("/x\r\nSet-Cookie: a=b"));
        assertFalse(GestorSesion.esDestinoInterno("/mis-pedidos/{id}"));
    }
}

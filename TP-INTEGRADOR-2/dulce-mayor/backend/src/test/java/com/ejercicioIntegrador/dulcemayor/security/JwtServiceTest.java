package com.ejercicioIntegrador.dulcemayor.security;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Persistencia de sesión (Ola 1): renovación del token con un límite máximo de sesión. */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-dulce-mayor-con-mas-de-32-caracteres";
    private static final long UNA_HORA = 3_600_000L;
    private static final long UN_DIA = 86_400_000L;

    private final JwtService jwt = new JwtService(SECRETO, UNA_HORA, UN_DIA);

    @Test
    void unTokenNuevoSePuedeRenovarConservandoElInicioDeLaSesion() {
        Date inicio = new Date(System.currentTimeMillis() - 2 * UNA_HORA);
        String token = jwt.generarToken("ana@kiosco.com", RolUsuario.CLIENTE, "u-1", inicio);

        Date conservado = jwt.inicioSesionRenovable(token).orElseThrow();

        assertEquals(inicio.getTime(), conservado.getTime());
        assertEquals("ana@kiosco.com", jwt.extraerCorreo(token));
    }

    @Test
    void unaSesionQueLlegoAlMaximoNoSeRenueva() {
        // Sesión iniciada hace más de 24 h: el token generado ya vence en el límite (no se extiende)
        Date inicio = new Date(System.currentTimeMillis() - UN_DIA - 1000);
        String token = jwt.generarToken("ana@kiosco.com", RolUsuario.CLIENTE, "u-1", inicio);

        assertTrue(jwt.inicioSesionRenovable(token).isEmpty());
        assertNull(jwt.extraerCorreo(token)); // vencido
    }

    @Test
    void unTokenAdulteradoNoSeRenueva() {
        String token = jwt.generarToken("ana@kiosco.com", RolUsuario.CLIENTE, "u-1");
        String adulterado = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");

        assertTrue(jwt.inicioSesionRenovable(adulterado).isEmpty());
        assertTrue(jwt.inicioSesionRenovable("no-es-un-token").isEmpty());
    }

    @Test
    void laSesionMaximaNuncaEsMenorQueLaDuracionDelToken() {
        JwtService mal = new JwtService(SECRETO, UNA_HORA, 1000);
        assertEquals(3600, mal.getSesionMaximaSegundos());
        assertEquals(86400, jwt.getSesionMaximaSegundos());
    }
}

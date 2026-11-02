package com.ejercicioIntegrador.dulcemayor.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Códigos de un solo uso para terminar el ingreso con Google/Facebook sin exponer el JWT en la URL.
 * Blockazo redirigía con ?token=...; acá la API redirige con ?codigo=... y el sitio institucional
 * lo canjea por el token llamando a la API de servidor a servidor.
 *  - 256 bits aleatorios (SecureRandom), válidos 60 segundos y canjeables una sola vez.
 *  - Se guardan en memoria: si la API corre en varias instancias, hay que moverlos a un almacén
 *    compartido (por ejemplo Redis o una tabla). Está documentado en CONFIGURACION-OAUTH.md.
 */
@Component
public class CodigosCanjeOAuth2 {

    static final long VIGENCIA_SEGUNDOS = 60;

    private final SecureRandom aleatorio = new SecureRandom();
    private final Map<String, Pendiente> pendientes = new ConcurrentHashMap<>();
    private final Clock reloj;

    public CodigosCanjeOAuth2() {
        this(Clock.systemUTC());
    }

    CodigosCanjeOAuth2(Clock reloj) {
        this.reloj = reloj;
    }

    /** Genera un código para el correo indicado. */
    public String emitir(String correo) {
        depurarVencidos();
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        pendientes.put(codigo, new Pendiente(correo, Instant.now(reloj).plusSeconds(VIGENCIA_SEGUNDOS)));
        return codigo;
    }

    /** Devuelve el correo y elimina el código; vacío si no existe, ya se usó o venció. */
    public Optional<String> canjear(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        Pendiente pendiente = pendientes.remove(codigo);
        if (pendiente == null || pendiente.expira().isBefore(Instant.now(reloj))) {
            return Optional.empty();
        }
        return Optional.of(pendiente.correo());
    }

    private void depurarVencidos() {
        Instant ahora = Instant.now(reloj);
        pendientes.entrySet().removeIf(entrada -> entrada.getValue().expira().isBefore(ahora));
    }

    private record Pendiente(String correo, Instant expira) {
    }
}

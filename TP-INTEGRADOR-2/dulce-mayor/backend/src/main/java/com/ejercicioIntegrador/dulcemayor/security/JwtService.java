package com.ejercicioIntegrador.dulcemayor.security;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Genera y valida los JWT (adaptado de Blockazo, librería jjwt).
 * El "subject" es el correo. El rol viaja como dato informativo para los clientes: los permisos
 * reales se recalculan en cada request a partir de la base (ver DetallesUsuarioService), así que
 * una baja, un cambio de clave o un cambio de rol tienen efecto inmediato.
 *
 * Persistencia de sesión (corrección Ola 1): cada token lleva "inicioSesion", el momento del ingreso.
 * Las apps pueden pedir un token nuevo (renovar) mientras el actual siga vigente, pero nunca más allá de
 * jwt.sesion-maxima desde el ingreso: así la sesión sobrevive a cerrar y abrir la app sin quedar abierta para siempre.
 */
@Service
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);
    private static final String PREFIJO_CLAVE_DESARROLLO = "clave-de-desarrollo";

    private final SecretKey clave;
    private static final String CLAIM_INICIO = "inicioSesion";

    private final long expiracionMs;
    private final long sesionMaximaMs;

    public JwtService(@Value("${jwt.secret}") String secreto, @Value("${jwt.expiration}") long expiracionMs,
                      @Value("${jwt.sesion-maxima:86400000}") long sesionMaximaMs) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres");
        }
        if (secreto.startsWith(PREFIJO_CLAVE_DESARROLLO)) {
            LOG.warn("Se está usando la clave JWT de desarrollo. Definí JWT_SECRET antes de desplegar.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
        this.sesionMaximaMs = Math.max(sesionMaximaMs, expiracionMs);
    }

    /** Token de un ingreso nuevo: la sesión empieza ahora. */
    public String generarToken(String correo, RolUsuario rol, String idUsuario) {
        return generarToken(correo, rol, idUsuario, new Date());
    }

    /**
     * Token que conserva el inicio de la sesión (renovación). Su vencimiento nunca supera el límite
     * de la sesión: inicioSesion + jwt.sesion-maxima.
     */
    public String generarToken(String correo, RolUsuario rol, String idUsuario, Date inicioSesion) {
        Date ahora = new Date();
        long vencimiento = Math.min(ahora.getTime() + expiracionMs, inicioSesion.getTime() + sesionMaximaMs);
        return Jwts.builder()
                .subject(correo)
                .claims(Map.of("rol", rol.name(), "idUsuario", idUsuario, CLAIM_INICIO, inicioSesion.getTime()))
                .issuedAt(ahora)
                .expiration(new Date(vencimiento))
                .signWith(clave)
                .compact();
    }

    /**
     * Inicio de la sesión del token si todavía se puede renovar (token válido y dentro de la sesión máxima),
     * o vacío si no. Los tokens emitidos antes de esta versión no tienen el dato: se toma su fecha de emisión.
     */
    public java.util.Optional<Date> inicioSesionRenovable(String token) {
        try {
            Claims claims = leerClaims(token);
            Number inicio = claims.get(CLAIM_INICIO, Number.class);
            Date desde = inicio == null ? claims.getIssuedAt() : new Date(inicio.longValue());
            if (desde == null || System.currentTimeMillis() - desde.getTime() >= sesionMaximaMs) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(desde);
        } catch (JwtException | IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    /** Correo del token, o null si el token es inválido, está vencido o fue adulterado. */
    public String extraerCorreo(String token) {
        try {
            return leerClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean esValido(String token, UserDetails usuario) {
        try {
            Claims claims = leerClaims(token);
            return claims.getSubject().equalsIgnoreCase(usuario.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    public long getSesionMaximaSegundos() {
        return sesionMaximaMs / 1000;
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

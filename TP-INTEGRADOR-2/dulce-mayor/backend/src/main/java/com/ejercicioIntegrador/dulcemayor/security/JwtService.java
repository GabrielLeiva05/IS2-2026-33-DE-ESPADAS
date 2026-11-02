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
 */
@Service
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);
    private static final String PREFIJO_CLAVE_DESARROLLO = "clave-de-desarrollo";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto, @Value("${jwt.expiration}") long expiracionMs) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres");
        }
        if (secreto.startsWith(PREFIJO_CLAVE_DESARROLLO)) {
            LOG.warn("Se está usando la clave JWT de desarrollo. Definí JWT_SECRET antes de desplegar.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String correo, RolUsuario rol, String idUsuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(correo)
                .claims(Map.of("rol", rol.name(), "idUsuario", idUsuario))
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
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

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

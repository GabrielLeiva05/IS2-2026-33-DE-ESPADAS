package com.ejercicioIntegrador.dulcemayor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y deja al usuario autenticado para esa request
 * (reutilizado de Blockazo). Las autoridades se cargan de la base en cada request.
 * No es un @Component a propósito: si lo fuera, Spring Boot también lo registraría como filtro
 * global del servlet y se ejecutaría dos veces. Se crea en SecurityConfig.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = encabezado.substring(7);
        String correo = jwtService.extraerCorreo(token);
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails usuario = userDetailsService.loadUserByUsername(correo);
                if (usuario.isEnabled() && jwtService.esValido(token, usuario)) {
                    UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (UsernameNotFoundException e) {
                // La cuenta del token ya no existe o fue dada de baja: la request sigue sin autenticar (401)
            }
        }
        filterChain.doFilter(request, response);
    }
}

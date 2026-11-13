package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.security.OAuth2ExitoHandler;
import com.ejercicioIntegrador.dulcemayor.security.OAuth2FalloHandler;
import com.ejercicioIntegrador.dulcemayor.service.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Ingreso de clientes con Google y Facebook (OAuth 2.0 / OpenID Connect).
 * Solo se activa con el perfil "oauth-google" y/o "oauth-facebook" (archivos application-oauth-*.properties),
 * así la API arranca sin credenciales externas, igual que el perfil auth0 de Blockazo.
 * Flujo: GET /oauth2/authorization/{google|facebook} -> proveedor -> /login/oauth2/code/{proveedor}
 *        -> OAuth2ExitoHandler -> redirección al sitio institucional con ?codigo=...
 */
@Configuration
@Profile({"oauth-google", "oauth-facebook"})
public class SeguridadOAuth2Config {

    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http, ServicioAutenticacion servicioAutenticacion,
                                                         @Value("${app.oauth2.redirect-cliente}") String redireccionCliente)
            throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // La sesión solo guarda el "state" de OAuth2 mientras dura el ingreso; luego se invalida
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth
                        .successHandler(new OAuth2ExitoHandler(servicioAutenticacion, redireccionCliente))
                        .failureHandler(new OAuth2FalloHandler(redireccionCliente)));
        return http.build();
    }
}

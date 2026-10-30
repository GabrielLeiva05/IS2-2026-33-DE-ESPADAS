package com.competencia.blockazo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad del backend:
 *  - Cadena 1 (/api/**): REST stateless con JWT.
 *  - Cadena 2 (/oauth2/**, /login/oauth2/**): flujo OAuth2 con Google / Auth0.
 *  - Cadena 3 (resto): frontend React (archivos estaticos + rutas del SPA), solo lectura (GET).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2LoginSuccessHandler oauth2SuccessHandler;
    private final OAuth2LoginFailureHandler oauth2FailureHandler;
    private final JsonSecurityErrorHandler jsonErrorHandler;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtService jwtService,
                          UserDetailsService userDetailsService,
                          AuthenticationProvider authenticationProvider,
                          OAuth2LoginSuccessHandler oauth2SuccessHandler,
                          OAuth2LoginFailureHandler oauth2FailureHandler,
                          JsonSecurityErrorHandler jsonErrorHandler,
                          @Value("${app.frontend.url}") String origenes) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationProvider = authenticationProvider;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.oauth2FailureHandler = oauth2FailureHandler;
        this.jsonErrorHandler = jsonErrorHandler;
        this.origenesPermitidos = Arrays.stream(origenes.split(",")).map(String::trim).toList();
    }

    // 1) API REST con JWT
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()          // register / login
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN") // ABM de usuarios
                .anyRequest().authenticated()                         // /api/perfil, etc.
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(jsonErrorHandler)   // 401 en JSON
                .accessDeniedHandler(jsonErrorHandler))       // 403 en JSON
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 2) Login con Google / Auth0 (OAuth2 + OpenID Connect)
    //    El frontend redirige el navegador a: GET /oauth2/authorization/google
    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // La sesion solo se usa para guardar el "state" de OAuth2 mientras dura el login
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .oauth2Login(oauth -> oauth
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            );

        return http.build();
    }

    // 3) Frontend React servido por Spring (index.html, /assets/**, rutas del SPA).
    //    Son archivos publicos: la proteccion real esta en la API (cadena 1), que exige el JWT.
    @Bean
    @Order(3)
    public SecurityFilterChain frontendSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().denyAll()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(jsonErrorHandler)
                .accessDeniedHandler(jsonErrorHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

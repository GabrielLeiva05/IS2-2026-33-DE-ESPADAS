package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.security.JwtAuthenticationFilter;
import com.ejercicioIntegrador.dulcemayor.security.JwtService;
import com.ejercicioIntegrador.dulcemayor.security.ManejadorSeguridadJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
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
 * Seguridad de la API (reemplaza la configuración provisoria de la etapa de Catálogo y Stock).
 *
 * Cadena 1 (/api/**): REST stateless con JWT y la matriz de permisos por rol (ver README).
 *   - JEFE hereda los permisos de ADMINISTRATIVO y PREVENTISTA (RoleHierarchy).
 *   - Un usuario con la clave temporal solo tiene CAMBIO_CLAVE_PENDIENTE: todo endpoint con rol responde 403.
 *   - Regla final denyAll(): un endpoint nuevo queda cerrado hasta que se le asigne un permiso.
 * Cadena 2 (OAuth2 Google/Facebook): en SeguridadOAuth2Config, activa solo si hay proveedores configurados.
 * Cadena 3 (resto): se rechaza; la API no sirve páginas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMINISTRATIVO = "ADMINISTRATIVO";
    private static final String JEFE = "JEFE";
    private static final String PREVENTISTA = "PREVENTISTA";
    private static final String DISTRIBUIDOR = "DISTRIBUIDOR";
    private static final String CLIENTE = "CLIENTE";

    /** Recursos del catálogo que gestiona el personal administrativo. */
    private static final String[] RECURSOS_CATALOGO = {"/api/categorias/**", "/api/subcategorias/**",
            "/api/productos/**", "/api/imagenes/**", "/api/vigencias-precio/**", "/api/stock/**"};
    private static final String[] RECURSOS_UBICACION = {"/api/paises/**", "/api/provincias/**",
            "/api/departamentos/**", "/api/localidades/**"};

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationProvider authenticationProvider;
    private final ManejadorSeguridadJson manejadorSeguridad;

    @Value("${app.cors.allowed-origins}")
    private String origenesPermitidos;

    public SecurityConfig(JwtService jwtService, UserDetailsService userDetailsService,
                          AuthenticationProvider authenticationProvider, ManejadorSeguridadJson manejadorSeguridad) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationProvider = authenticationProvider;
        this.manejadorSeguridad = manejadorSeguridad;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Autenticación ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro",
                                "/api/auth/oauth2/canje").permitAll()
                        .requestMatchers("/api/auth/yo", "/api/auth/clave").authenticated() // incluso con clave pendiente

                        // --- Público: catálogo, imágenes de productos y clasificación ---
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**", "/api/imagenes/*/contenido",
                                "/api/categorias/**", "/api/subcategorias/**").permitAll()
                        // --- Público: sucursales activas y cotización informativa del dólar ---
                        .requestMatchers(HttpMethod.GET, "/api/sucursales", "/api/cotizacion-dolar").permitAll()

                        // --- Webhook de Mercado Pago: llega sin JWT, se autentica con la firma HMAC ---
                        .requestMatchers(HttpMethod.POST, "/api/pagos/mercadopago/notificacion").permitAll()

                        // --- Preventista: cartera propia, pedidos y disponibilidad de stock ---
                        .requestMatchers("/api/preventista/puntos-venta/**", "/api/preventista/pedidos/**",
                                "/api/preventista/visitas/**")
                                .hasAnyRole(PREVENTISTA, JEFE)
                        // --- Pagos: conciliación de transferencias, cobro en efectivo y consulta ---
                        .requestMatchers("/api/pagos/**").hasRole(ADMINISTRATIVO)
                        // --- Promociones: administración de códigos y vista previa ---
                        .requestMatchers("/api/promociones/**").hasRole(ADMINISTRATIVO)
                        // --- Rutas: asignación de pedidos a la jornada de reparto ---
                        .requestMatchers("/api/rutas/**").hasRole(ADMINISTRATIVO)
                        // --- Distribuidor: solo su propia hoja de ruta ---
                        .requestMatchers("/api/distribuidor/**").hasAnyRole(DISTRIBUIDOR, ADMINISTRATIVO)
                        // --- Sucursales y sucursal completa: gestión administrativa ---
                        .requestMatchers("/api/sucursales/**").hasRole(ADMINISTRATIVO)
                        // --- Portal del cliente: solo sus pedidos y comprobantes ---
                        .requestMatchers("/api/cliente/**").hasRole(CLIENTE)
                        // --- Pedidos: bandeja, pagos y cierre autorizado (consumo o liberación) ---
                        .requestMatchers("/api/pedidos/**").hasRole(ADMINISTRATIVO)
                        .requestMatchers(HttpMethod.GET, "/api/stock/producto/*/disponibilidad")
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").hasAnyRole(ADMINISTRATIVO, PREVENTISTA)

                        // --- Catálogo y stock: personal administrativo (el jefe lo hereda) ---
                        .requestMatchers(RECURSOS_CATALOGO).hasRole(ADMINISTRATIVO)

                        // --- Ubicación: lectura para empleados, gestión para administrativos ---
                        .requestMatchers(HttpMethod.GET, RECURSOS_UBICACION)
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA, DISTRIBUIDOR)
                        .requestMatchers(RECURSOS_UBICACION).hasRole(ADMINISTRATIVO)

                        // --- Personas y usuarios ---
                        .requestMatchers("/api/empleados/**", "/api/usuarios/**").hasRole(JEFE)
                        .requestMatchers("/api/clientes/yo").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasRole(ADMINISTRATIVO)
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole(JEFE)

                        // --- Todo lo demás queda cerrado hasta que su módulo defina permisos ---
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(manejadorSeguridad)
                        .accessDeniedHandler(manejadorSeguridad))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Cualquier ruta que no sea de la API ni del flujo OAuth2 se rechaza. */
    @Bean
    @Order(3)
    public SecurityFilterChain restoSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(manejadorSeguridad)
                        .accessDeniedHandler(manejadorSeguridad));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setExposedHeaders(List.of("Location", ManejadorSeguridadJson.ENCABEZADO_ACCION));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}

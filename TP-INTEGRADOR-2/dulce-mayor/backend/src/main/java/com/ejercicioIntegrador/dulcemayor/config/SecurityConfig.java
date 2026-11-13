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
                        .requestMatchers("/api/auth/yo", "/api/auth/clave", "/api/auth/renovar").authenticated() // incluso con clave pendiente

                        // --- Pagos: webhook público de Mercado Pago (el estado real se valida contra su API) ---
                        .requestMatchers(HttpMethod.POST, "/api/pagos/mercadopago/webhook").permitAll()

                        // --- Público: catálogo, imágenes de productos y clasificación ---
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**", "/api/imagenes/*/contenido",
                                "/api/categorias/**", "/api/subcategorias/**", "/api/cotizacion").permitAll()

                        // --- Preventista: consultar productos y disponibilidad de stock ---
                        .requestMatchers(HttpMethod.GET, "/api/stock/producto/*/disponibilidad")
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").hasAnyRole(ADMINISTRATIVO, PREVENTISTA)

                        // --- Facturación (Ola 3). El cliente solo ve las suyas (lo verifica el servicio) ---
                        .requestMatchers(HttpMethod.GET, "/api/facturas/mis-facturas/**").hasRole(CLIENTE)
                        .requestMatchers("/api/facturas/**", "/api/proveedores/**", "/api/promociones/**").hasRole(ADMINISTRATIVO)

                        // --- Estadísticas del panel (HU-43) ---
                        .requestMatchers(HttpMethod.GET, "/api/estadisticas/pedidos-por-preventista").hasRole(JEFE)
                        .requestMatchers(HttpMethod.GET, "/api/estadisticas/stock-por-categoria").hasRole(ADMINISTRATIVO)

                        // --- Catálogo y stock: personal administrativo (el jefe lo hereda) ---
                        .requestMatchers(RECURSOS_CATALOGO).hasRole(ADMINISTRATIVO)

                        // --- Ubicación: lectura para empleados, gestión para administrativos ---
                        .requestMatchers(HttpMethod.GET, RECURSOS_UBICACION)
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA, DISTRIBUIDOR)
                        .requestMatchers(RECURSOS_UBICACION).hasRole(ADMINISTRATIVO)

                        // --- Personas y usuarios ---
                        // El administrativo lista preventistas y distribuidores para armar rutas y asignar entregas.
                        .requestMatchers(HttpMethod.GET, "/api/empleados/rol/*").hasRole(ADMINISTRATIVO)
                        .requestMatchers("/api/empleados/**", "/api/usuarios/**").hasRole(JEFE)
                        .requestMatchers("/api/clientes/yo").hasRole(CLIENTE)
                        .requestMatchers("/api/presupuestos/**").hasRole(CLIENTE) // asistente de presupuestos
                        // Alta de clientes desde el celular del preventista y búsqueda por correo
                        .requestMatchers(HttpMethod.POST, "/api/clientes/campo").hasAnyRole(ADMINISTRATIVO, PREVENTISTA)
                        .requestMatchers(HttpMethod.GET, "/api/clientes/buscar").hasAnyRole(ADMINISTRATIVO, PREVENTISTA)
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasRole(ADMINISTRATIVO)
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole(JEFE)

                        // --- Puntos de venta (HU-17) ---
                        .requestMatchers(HttpMethod.GET, "/api/puntos-venta/**")
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA, DISTRIBUIDOR)
                        .requestMatchers(HttpMethod.POST, "/api/puntos-venta/**").hasAnyRole(ADMINISTRATIVO, PREVENTISTA)
                        .requestMatchers(HttpMethod.PUT, "/api/puntos-venta/**").hasRole(ADMINISTRATIVO) // el preventista da de alta, no edita
                        .requestMatchers("/api/puntos-venta/**").hasRole(ADMINISTRATIVO)

                        // --- Mapas: geocodificación para el alta de puntos de venta ---
                        .requestMatchers(HttpMethod.GET, "/api/mapas/**")
                                .hasAnyRole(ADMINISTRATIVO, PREVENTISTA, DISTRIBUIDOR)

                        // --- Rutas: la app del preventista usa solo /mia (su ruta); el resto es administración ---
                        .requestMatchers("/api/rutas/mia/**").hasRole(PREVENTISTA)
                        .requestMatchers("/api/rutas/**").hasRole(ADMINISTRATIVO)

                        // --- Reportes administrativos y gerenciales: solo el jefe administrativo ---
                        .requestMatchers("/api/reportes/**").hasRole(JEFE)

                        // --- Pedidos: cada rol en su sección; la propiedad la verifica ServicioPedido ---
                        .requestMatchers("/api/pedidos/mis-pedidos/**").hasRole(CLIENTE)
                        .requestMatchers("/api/pedidos/preventista/**").hasRole(PREVENTISTA)
                        .requestMatchers("/api/pedidos/entregas/**").hasRole(DISTRIBUIDOR)
                        .requestMatchers("/api/pedidos/**").hasRole(ADMINISTRATIVO)

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

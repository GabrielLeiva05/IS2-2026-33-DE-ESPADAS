package com.ejercicioIntegrador.tiendaderopa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/logincheck",
                    "/logout", "/registrar", "/registro", "/").permitAll()
                .requestMatchers("/webhooks/mercadopago").permitAll()
                .requestMatchers("/carrito", "/carrito/**", "/pago/resultado", "/mis-ordenes/**").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/productos").permitAll()
                .requestMatchers(HttpMethod.GET, "/imagen/**").permitAll()
                        .requestMatchers("/perfil", "/perfil/**")
                            .hasRole("CLIENTE")
                .requestMatchers("/admin/**")
                    .hasRole("ADMINISTRATIVO")
                .requestMatchers("/categorias/**", "/subcategorias/**",
                    "/formulario/**", "/eliminar/**", "/ordenesCompraProveedor/**",
                    "/facturas/**", "/imagen/**", "/pais/**", "/provincia/**")
                    .hasRole("ADMINISTRATIVO")
                        .anyRequest().hasRole("ADMINISTRATIVO")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        // Redirección dinámica según el rol
                        .successHandler((request, response, authentication) -> {
                            boolean esAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATIVO"));

                            if (esAdmin) {
                                response.sendRedirect("/admin/dashboard"); // Ajusta esta ruta a la de tu controlador admin
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/webhooks/mercadopago"));

        return http.build();
    }
}
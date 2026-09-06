package com.uncuyo.tp1_ej4.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Encriptador de contrasenas. BCrypt genera un hash distinto cada vez
     * (usa un "salt" aleatorio incorporado), asi que dos usuarios con la
     * misma contrasena van a tener hashes distintos en la base. Se usa
     * tanto para guardar contrasenas nuevas como para validarlas en el login
     * (passwordEncoder.matches(...), que hace Spring Security internamente).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos estaticos: siempre publicos
                        // (/imagenes/** son las imagenes de portada de los videojuegos,
                        // servidas por ImagenConfiguration - se ven en el catalogo publico)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/imagenes/**", "/assets/**", "/webjars/**")
                        .permitAll()

                        // Paginas publicas de la tienda (catalogo, detalle, busqueda) y login
                        .requestMatchers("/", "/inicio", "/detalle/**", "/busqueda", "/login")
                        .permitAll()

                        // Panel de administracion (ABM de videojuegos/categorias/estudios):
                        // solo usuarios con rol ADMIN
                        .requestMatchers(
                                "/crud", "/crud/**",
                                "/categorias", "/categorias/**",
                                "/estudios", "/estudios/**",
                                "/formulario/**",
                                "/eliminar/**"
                        ).hasRole("ADMIN")

                        // Cualquier otra ruta que se agregue en el futuro: requiere estar logueado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        // CSRF queda habilitado (comportamiento por defecto, recomendado).
        // No hace falta tocar los formularios existentes porque todos usan
        // th:action, y Spring Security le agrega el token automaticamente
        // a los forms renderizados asi por Thymeleaf.

        return http.build();
    }
}

package com.ejercicioIntegrador.tiendaderopa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                        // Rutas públicas y recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/registrar", "/registro").permitAll()
                        // Rutas exclusivas para ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        // El resto de rutas requieren autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        // Redirección dinámica según el rol
                        .successHandler((request, response, authentication) -> {
                            boolean esAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

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
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
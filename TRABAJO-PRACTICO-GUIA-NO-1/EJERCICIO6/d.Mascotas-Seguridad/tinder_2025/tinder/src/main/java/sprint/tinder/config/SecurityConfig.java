package sprint.tinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    // BCrypt: hash de un solo sentido + "salt" aleatorio interno.
    // Nunca se puede "desencriptar", solo se puede volver a hashear y comparar (matches()).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Páginas y recursos públicos (no requieren estar logueado)
                .requestMatchers(
                        "/", "/inicio", "/login", "/registro", "/registrar",
                        "/usuario/registrar", "/usuario/loginUsuario",
                        "/error", "/foto/**",
                        "/css/**", "/js/**", "/img/**", "/vendor/**"
                ).permitAll()
                // Panel de administración: solo usuarios con rol ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Todo lo demás (mis-mascotas, votar, perfil, etc.) requiere estar logueado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")                     // usa el login.html ya existente
                .loginProcessingUrl("/usuario/loginUsuario") // misma URL que ya usaba el <form>
                .usernameParameter("email")               // el input se llama "email" en login.html
                .passwordParameter("clave")               // el input se llama "clave" en login.html
                .defaultSuccessUrl("/mascota/mis-mascotas", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout((LogoutConfigurer<HttpSecurity> logout) -> logout
                // El link existente es <a href="/logout"> (GET), por eso se habilita GET acá
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/inicio?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // NOTA (básico/didáctico): se deshabilita CSRF porque varios formularios del
            // proyecto (mascota.html con formaction/formmethod, mascotas-explorar.html, etc.)
            // no usan th:action, que es lo que Thymeleaf necesita para inyectar el token
            // automáticamente. En un proyecto real conviene dejar CSRF activo y agregar
            // <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            // a cada <form> POST, o convertir action="" en th:action="@{...}".
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

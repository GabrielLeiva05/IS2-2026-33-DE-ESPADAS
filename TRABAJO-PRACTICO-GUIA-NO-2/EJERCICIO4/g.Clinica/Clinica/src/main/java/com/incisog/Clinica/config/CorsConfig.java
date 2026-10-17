package com.incisog.Clinica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: el navegador bloquea por seguridad que una página de un "origen" (React en localhost:5173)
 * llame a otro origen (el backend en localhost:8080), salvo que el backend lo autorice.
 * Postman no es un navegador, por eso ahí nunca apareció este problema.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")   // el servidor de desarrollo de Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}

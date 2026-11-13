package com.dulcemayor.institucional.config;

import com.dulcemayor.institucional.seguridad.InterceptorAcceso;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorAcceso interceptorAcceso;

    public ConfiguracionWeb(InterceptorAcceso interceptorAcceso) {
        this.interceptorAcceso = interceptorAcceso;
    }

    /** "Mi cuenta", "Mis pedidos" y "Mis facturas" requieren sesión; el resto del sitio es público. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorAcceso).addPathPatterns("/mi-cuenta", "/mi-cuenta/**", "/mis-pedidos", "/mis-pedidos/**",
                "/mis-facturas", "/mis-facturas/**");
    }
}

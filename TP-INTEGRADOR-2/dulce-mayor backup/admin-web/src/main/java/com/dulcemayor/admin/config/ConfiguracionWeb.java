package com.dulcemayor.admin.config;

import com.dulcemayor.admin.seguridad.InterceptorAcceso;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorAcceso interceptorAcceso;

    public ConfiguracionWeb(InterceptorAcceso interceptorAcceso) {
        this.interceptorAcceso = interceptorAcceso;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorAcceso)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/css/**", "/js/**", "/img/**", "/favicon.svg", "/error");
    }
}

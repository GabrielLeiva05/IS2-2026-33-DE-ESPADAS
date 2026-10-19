package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Rol;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mapa de acceso de la aplicacion (el orden de registro es el orden de ejecucion):
 *   1) SesionInterceptor     : todas las paginas -> refresca al usuario desde la BD.
 *   2) /admin/**             : solo ADMIN.
 *   3) /carrito/**           : cualquier usuario logueado.
 *   El resto (catalogo, login, registro, imagenes) es publico.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SesionInterceptor sesionInterceptor;

    public WebConfig(SesionInterceptor sesionInterceptor) {
        this.sesionInterceptor = sesionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/assets/**", "/imagenes/**", "/error", "/favicon.ico");

        registry.addInterceptor(new AutorizacionInterceptor(Rol.ADMIN))
                .addPathPatterns("/admin/**");

        registry.addInterceptor(new AutorizacionInterceptor(null))
                .addPathPatterns("/carrito/**");
    }
}

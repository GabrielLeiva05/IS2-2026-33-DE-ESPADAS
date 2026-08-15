//Paquetes
package com.example.uml_con_orm.config;

//Imports
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/* CORS es un mecanismo de seguridad que restringe peticiones HTTP.
   Cuando abramos el index.html en el navegador, de forma local, estaremos
   en el puerto 5500, mientras que el del backend está en el puerto 8080.
   Esta configuración es para que no se bloqueen esas peticiones HTTP.
*/


@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}

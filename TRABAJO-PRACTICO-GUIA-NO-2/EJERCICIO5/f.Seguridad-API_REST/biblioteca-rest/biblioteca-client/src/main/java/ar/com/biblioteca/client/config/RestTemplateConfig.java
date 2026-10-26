package ar.com.biblioteca.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Define el bean RestTemplate que usa toda la aplicación cliente para comunicarse con el servidor.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @Qualifier("apiRestTemplate")
    public RestTemplate apiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // ms para establecer la conexión
        factory.setReadTimeout(10000);     // ms para recibir la respuesta
        return new RestTemplate(factory);
    }

    @Bean
    @Qualifier("authenticationRestTemplate")
    public RestTemplate authenticationRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}

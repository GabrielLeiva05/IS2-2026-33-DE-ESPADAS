package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.security.OAuth2ExitoHandler;
import com.ejercicioIntegrador.dulcemayor.security.OAuth2FalloHandler;
import com.ejercicioIntegrador.dulcemayor.service.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Ingreso de clientes con Google y Facebook (OAuth 2.0 / OpenID Connect).
 *
 * <p>Se activa con {@code app.oauth2.habilitado=true}, que puede definirse en el archivo {@code .env}
 * ({@code APP_OAUTH2_HABILITADO=true}) junto con las credenciales del proveedor. No hace falta activar
 * perfiles de Spring ni variables de entorno del sistema operativo. Con la bandera apagada —el valor
 * por defecto— la API arranca sin credenciales externas y el sitio institucional no muestra los botones
 * sociales.</p>
 *
 * <p>Flujo: GET /oauth2/authorization/{google|facebook} → proveedor → /login/oauth2/code/{proveedor}
 * → OAuth2ExitoHandler → redirección al sitio institucional con {@code ?codigo=…} para canjear.</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.oauth2.habilitado", havingValue = "true")
public class SeguridadOAuth2Config {

    private static final String REDIRECCION = "{baseUrl}/login/oauth2/code/{registrationId}";

    /**
     * Registro de proveedores armado en código: solo se incluyen los que tienen credenciales
     * completas, así una configuración a medias no rompe el arranque.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.oauth2.google.client-id:}") String googleId,
            @Value("${app.oauth2.google.client-secret:}") String googleSecret,
            @Value("${app.oauth2.facebook.client-id:}") String facebookId,
            @Value("${app.oauth2.facebook.client-secret:}") String facebookSecret) {
        List<ClientRegistration> registros = new ArrayList<>();
        if (completo(googleId, googleSecret)) {
            registros.add(google(googleId, googleSecret));
        }
        if (completo(facebookId, facebookSecret)) {
            registros.add(facebook(facebookId, facebookSecret));
        }
        if (registros.isEmpty()) {
            throw new IllegalStateException(
                    "app.oauth2.habilitado=true pero no hay credenciales completas de Google o Facebook. "
                            + "Definí GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET o FACEBOOK_CLIENT_ID/"
                            + "FACEBOOK_CLIENT_SECRET en el archivo .env.");
        }
        return new InMemoryClientRegistrationRepository(registros);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http,
                                                         ServicioAutenticacion servicioAutenticacion,
                                                         @Value("${app.oauth2.redirect-cliente}") String redireccionCliente)
            throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // La sesión solo guarda el "state" de OAuth2 mientras dura el ingreso; luego se invalida
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth
                        .successHandler(new OAuth2ExitoHandler(servicioAutenticacion, redireccionCliente))
                        .failureHandler(new OAuth2FalloHandler(redireccionCliente)));
        return http.build();
    }

    private static boolean completo(String id, String secreto) {
        return id != null && !id.isBlank() && secreto != null && !secreto.isBlank();
    }

    private static ClientRegistration google(String id, String secreto) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(id)
                .clientSecret(secreto)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECCION)
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Google")
                .build();
    }

    private static ClientRegistration facebook(String id, String secreto) {
        return ClientRegistration.withRegistrationId("facebook")
                .clientId(id)
                .clientSecret(secreto)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECCION)
                .scope("public_profile", "email")
                .authorizationUri("https://www.facebook.com/v19.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v19.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,first_name,last_name")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
    }
}

package ar.com.biblioteca.client.api;

import ar.com.biblioteca.client.exception.ApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private static final long REFRESH_BEFORE_EXPIRY_SECONDS = 30;

    private final RestTemplate restTemplate;
    private final String loginUrl;
    private final String username;
    private final String password;
    private volatile CachedToken cachedToken;

    public JwtTokenProvider(@Qualifier("authenticationRestTemplate") RestTemplate restTemplate,
                            @Value("${biblioteca.api.base-url}") String baseUrl,
                            @Value("${biblioteca.api.username}") String username,
                            @Value("${biblioteca.api.password}") String password) {
        this.restTemplate = restTemplate;
        this.loginUrl = baseUrl + "/auth/login";
        this.username = username;
        this.password = password;
    }

    public String getAccessToken() {
        CachedToken token = cachedToken;
        if (token != null && token.expiresAt().isAfter(Instant.now().plusSeconds(REFRESH_BEFORE_EXPIRY_SECONDS))) {
            return token.value();
        }
        synchronized (this) {
            token = cachedToken;
            if (token == null || !token.expiresAt().isAfter(
                    Instant.now().plusSeconds(REFRESH_BEFORE_EXPIRY_SECONDS))) {
                TokenResponse response;
                try {
                    response = restTemplate.postForObject(loginUrl,
                            new HttpEntity<>(new LoginRequest(username, password)), TokenResponse.class);
                } catch (HttpStatusCodeException exception) {
                    throw new ApiException(exception.getStatusCode().value(),
                            "El servidor rechazó la autenticación del cliente. Revise las credenciales configuradas.");
                }
                if (response == null || response.accessToken() == null || response.expiresIn() <= 0) {
                    throw new IllegalStateException("El servidor no devolvió un token JWT válido.");
                }
                token = new CachedToken(response.accessToken(), Instant.now().plusSeconds(response.expiresIn()));
                cachedToken = token;
            }
            return token.value();
        }
    }

    public synchronized void invalidateAccessToken() {
        cachedToken = null;
    }

    private record LoginRequest(String username, String password) {
    }

    private record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}

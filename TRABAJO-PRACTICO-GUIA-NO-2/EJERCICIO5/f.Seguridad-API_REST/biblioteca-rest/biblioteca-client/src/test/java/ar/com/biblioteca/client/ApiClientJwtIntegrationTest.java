package ar.com.biblioteca.client;

import ar.com.biblioteca.client.api.ApiClient;
import ar.com.biblioteca.client.api.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiClientJwtIntegrationTest {

    @Test
    void autenticaUnaVezYEnviaJwtEnLasLlamadasALaApi() {
        RestTemplate authenticationRestTemplate = new RestTemplate();
        MockRestServiceServer authenticationServer = MockRestServiceServer
                .bindTo(authenticationRestTemplate).build();
        authenticationServer.expect(requestTo("http://localhost:8080/api/v1/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"accessToken\":\"token-de-prueba\",\"tokenType\":\"Bearer\",\"expiresIn\":900}",
                        MediaType.APPLICATION_JSON));

        RestTemplate apiRestTemplate = new RestTemplate();
        MockRestServiceServer apiServer = MockRestServiceServer.bindTo(apiRestTemplate).build();
        apiServer.expect(requestTo("http://localhost:8080/api/v1/autores"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        apiServer.expect(requestTo("http://localhost:8080/api/v1/localidades"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(MockRestResponseCreators.withUnauthorizedRequest());
        authenticationServer.expect(requestTo("http://localhost:8080/api/v1/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"accessToken\":\"token-renovado\",\"tokenType\":\"Bearer\",\"expiresIn\":900}",
                        MediaType.APPLICATION_JSON));
        apiServer.expect(requestTo("http://localhost:8080/api/v1/localidades"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-renovado"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        JwtTokenProvider tokenProvider = new JwtTokenProvider(
                authenticationRestTemplate, "http://localhost:8080/api/v1", "cliente", "clave");
        ApiClient apiClient = new ApiClient(apiRestTemplate, tokenProvider, new ObjectMapper(),
                "http://localhost:8080/api/v1");

        assertThat(apiClient.getList("/autores", String[].class)).isEmpty();
        assertThat(apiClient.getList("/localidades", String[].class)).isEmpty();
        authenticationServer.verify();
        apiServer.verify();
    }
}

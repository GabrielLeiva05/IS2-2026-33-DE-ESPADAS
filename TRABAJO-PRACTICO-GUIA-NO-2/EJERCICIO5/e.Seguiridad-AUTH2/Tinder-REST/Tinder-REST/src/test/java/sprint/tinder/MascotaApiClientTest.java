package sprint.tinder;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import sprint.tinder.services.MascotaApiClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MascotaApiClientTest {
    @Test
    void sendsBasicAuthenticationAndReadsMascotaJson() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        String credentials = Base64.getEncoder().encodeToString(
                "ana@example.com:clave".getBytes(StandardCharsets.UTF_8));
        server.expect(requestTo("http://localhost/api/mascotas/mias"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic " + credentials))
                .andRespond(withSuccess("[{\"id\":\"pet-1\",\"nombre\":\"Luna\"}]", MediaType.APPLICATION_JSON));

        MascotaApiClient client = new MascotaApiClient(restTemplate, "http://localhost/api");
        var mascotas = client.listarMisMascotas("ana@example.com", "clave");

        assertEquals(1, mascotas.size());
        assertEquals("pet-1", mascotas.get(0).getId());
        assertEquals("Luna", mascotas.get(0).getNombre());
        server.verify();
    }
}

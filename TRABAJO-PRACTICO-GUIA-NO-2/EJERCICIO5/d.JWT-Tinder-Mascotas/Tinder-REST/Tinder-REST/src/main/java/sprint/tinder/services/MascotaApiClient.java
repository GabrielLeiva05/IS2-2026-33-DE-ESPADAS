package sprint.tinder.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sprint.tinder.dtos.MascotaDto;

import java.util.Arrays;
import java.util.List;

@Service
public class MascotaApiClient {
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public MascotaApiClient(RestTemplate restTemplate,
                            @Value("${tinder.api.base-url:http://localhost:9000/api}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    public List<MascotaDto> listarMisMascotas(String email, String clave) {
        HttpEntity<Void> request = new HttpEntity<>(headers(email, clave));
        ResponseEntity<MascotaDto[]> response = restTemplate.exchange(
                apiBaseUrl + "/mascotas/mias", HttpMethod.GET, request, MascotaDto[].class);
        MascotaDto[] mascotas = response.getBody();
        return mascotas == null ? List.of() : Arrays.asList(mascotas);
    }

    private HttpHeaders headers(String email, String clave) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, clave);
        return headers;
    }
}

package ar.com.biblioteca.client.service;

import ar.com.biblioteca.client.api.ApiClient;
import ar.com.biblioteca.client.dto.LocalidadDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consume /api/v1/localidades del servidor. */
@Service
public class LocalidadService {

    private final ApiClient api;

    public LocalidadService(ApiClient api) {
        this.api = api;
    }

    public List<LocalidadDTO> listar() {
        return api.getList("/localidades", LocalidadDTO[].class);
    }

    public LocalidadDTO obtener(Long id) {
        return api.get("/localidades/{id}", LocalidadDTO.class, id);
    }

    public LocalidadDTO crear(LocalidadDTO dto) {
        return api.post("/localidades", dto, LocalidadDTO.class);
    }

    public void actualizar(Long id, LocalidadDTO dto) {
        api.put("/localidades/{id}", dto, id);
    }

    public void eliminar(Long id) {
        api.delete("/localidades/{id}", id);
    }
}

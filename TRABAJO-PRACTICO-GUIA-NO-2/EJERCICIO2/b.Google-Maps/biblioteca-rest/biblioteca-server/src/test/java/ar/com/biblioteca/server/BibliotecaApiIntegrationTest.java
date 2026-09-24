package ar.com.biblioteca.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del recorrido principal de la API:
 * Localidad -> Persona (con Domicilio) -> Autor -> Libro (de la Persona) y sus reglas de integridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BibliotecaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void recorridoCompletoDeLaApi() throws Exception {
        // 1) Los datos de ejemplo están cargados
        mockMvc.perform(get("/api/v1/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 2) Alta de una localidad
        long localidadId = idDe(mockMvc.perform(post("/api/v1/localidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"denominacion\":\"Localidad de prueba\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        // 3) Alta de una persona con su domicilio (relación 1-1 y Domicilio -> Localidad)
        String personaJson = "{\"nombre\":\"Test\",\"apellido\":\"Unitario\",\"dni\":11111111,"
                + "\"domicilio\":{\"calle\":\"Calle Falsa\",\"numero\":123,\"localidad\":{\"id\":" + localidadId + "}}}";
        long personaId = idDe(mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domicilio.localidad.denominacion", is("Localidad de prueba")))
                .andReturn());

        // 4) DNI repetido => 409
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(personaJson))
                .andExpect(status().isConflict());

        // 5) Datos inválidos => 400 con detalle por campo
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"apellido\":\"X\",\"dni\":22222222,"
                                + "\"domicilio\":{\"calle\":\"C\",\"numero\":1,\"localidad\":{\"id\":" + localidadId + "}}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());

        // 6) Alta de un autor y de un libro de la persona (composición + Libro *-* Autor)
        long autorId = idDe(mockMvc.perform(post("/api/v1/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Autora\",\"apellido\":\"De Prueba\",\"biografia\":\"Bio\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        long libroId = idDe(mockMvc.perform(post("/api/v1/personas/" + personaId + "/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro de prueba\",\"fecha\":2020,\"genero\":\"Ensayo\",\"paginas\":100,"
                                + "\"autores\":[{\"id\":" + autorId + "}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autores", hasSize(1)))
                .andExpect(jsonPath("$.autor", is("Autora De Prueba")))
                .andReturn());

        mockMvc.perform(get("/api/v1/personas/" + personaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libros", hasSize(1)));

        // 7) No se puede borrar una localidad ni un autor que están en uso => 409
        mockMvc.perform(delete("/api/v1/localidades/" + localidadId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/autores/" + autorId)).andExpect(status().isConflict());

        // 8) Se elimina el libro y luego el autor ya puede borrarse
        mockMvc.perform(delete("/api/v1/personas/" + personaId + "/libros/" + libroId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/autores/" + autorId)).andExpect(status().isNoContent());

        // 9) Al eliminar la persona se elimina su domicilio y luego la localidad queda libre
        mockMvc.perform(delete("/api/v1/personas/" + personaId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/localidades/" + localidadId)).andExpect(status().isNoContent());

        // 10) Recurso inexistente => 404
        mockMvc.perform(get("/api/v1/personas/" + personaId)).andExpect(status().isNotFound());
    }

    private long idDe(MvcResult resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}

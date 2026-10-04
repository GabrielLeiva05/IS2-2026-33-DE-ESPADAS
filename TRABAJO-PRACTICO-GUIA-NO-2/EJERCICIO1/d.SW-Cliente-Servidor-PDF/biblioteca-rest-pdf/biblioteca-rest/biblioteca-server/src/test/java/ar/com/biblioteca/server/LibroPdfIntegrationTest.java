package ar.com.biblioteca.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de la carga y consulta del PDF de un libro.
 * Usa una carpeta de prueba bajo target/ para no escribir en C:/biblioteca.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "biblioteca.storage.pdf-dir=target/test-biblioteca")
class LibroPdfIntegrationTest {

    private static final Path CARPETA = Paths.get("target/test-biblioteca").toAbsolutePath().normalize();
    private static final byte[] PDF = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n"
            .getBytes(StandardCharsets.ISO_8859_1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cargaYConsultaDelPdfDeUnLibro() throws Exception {
        long localidadId = idDe(mockMvc.perform(post("/api/v1/localidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"denominacion\":\"Localidad PDF\"}")).andReturn());
        long personaId = idDe(mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Pdf\",\"apellido\":\"Tester\",\"dni\":44444444,"
                        + "\"domicilio\":{\"calle\":\"C\",\"numero\":1,\"localidad\":{\"id\":" + localidadId + "}}}"))
                .andExpect(status().isCreated()).andReturn());
        try {
            MockMultipartFile datos = new MockMultipartFile("libro", "", MediaType.APPLICATION_JSON_VALUE,
                    ("{\"titulo\":\"Cien años de soledad\",\"fecha\":1967,\"genero\":\"Novela\",\"paginas\":471}")
                            .getBytes(StandardCharsets.UTF_8));

            // 1) Alta con PDF => 201, tienePdf=true y el nombre del archivo NO se expone
            MvcResult creado = mockMvc.perform(multipart("/api/v1/personas/" + personaId + "/libros")
                            .file(datos)
                            .file(new MockMultipartFile("archivo", "cualquier.pdf", "application/pdf", PDF)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tienePdf", is(true)))
                    .andExpect(jsonPath("$.archivoPdf").doesNotExist())
                    .andReturn();
            long libroId = idDe(creado);

            // 2) El archivo quedó en disco con el nombre libro_<titulo>_<id>.pdf
            Path enDisco = CARPETA.resolve("libro_cien_anos_de_soledad_" + libroId + ".pdf");
            assertTrue(Files.exists(enDisco), "No se creó " + enDisco);
            assertArrayEquals(PDF, Files.readAllBytes(enDisco));

            // 3) Consulta: se devuelve como application/pdf y "inline" (se abre en el navegador)
            mockMvc.perform(get("/api/v1/personas/" + personaId + "/libros/" + libroId + "/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(content().bytes(PDF))
                    .andExpect(header().string("Content-Disposition", startsWith("inline")));

            // 4) Un archivo que no es PDF (aunque diga serlo) => 400 y no se crea el libro
            mockMvc.perform(multipart("/api/v1/personas/" + personaId + "/libros")
                            .file(datos)
                            .file(new MockMultipartFile("archivo", "falso.pdf", "application/pdf",
                                    "esto no es un pdf".getBytes(StandardCharsets.UTF_8))))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(get("/api/v1/personas/" + personaId + "/libros"))
                    .andExpect(jsonPath("$.length()", is(1)));

            // 5) Un libro sin PDF => 404 al pedir su PDF
            long sinPdfId = idDe(mockMvc.perform(post("/api/v1/personas/" + personaId + "/libros")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Sin PDF\",\"fecha\":2000,\"genero\":\"Ensayo\",\"paginas\":10}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tienePdf", is(false)))
                    .andReturn());
            mockMvc.perform(get("/api/v1/personas/" + personaId + "/libros/" + sinPdfId + "/pdf"))
                    .andExpect(status().isNotFound());

            // 6) Al eliminar el libro se elimina también su PDF del disco
            mockMvc.perform(delete("/api/v1/personas/" + personaId + "/libros/" + libroId))
                    .andExpect(status().isNoContent());
            assertFalse(Files.exists(enDisco), "El PDF debería haberse eliminado junto con el libro");
        } finally {
            mockMvc.perform(delete("/api/v1/personas/" + personaId));
            mockMvc.perform(delete("/api/v1/localidades/" + localidadId));
        }
    }

    private long idDe(MvcResult resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}

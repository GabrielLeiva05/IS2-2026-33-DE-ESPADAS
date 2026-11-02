package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.repository.RepositorioUsuario;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de punta a punta con la cadena de Spring Security real y H2 en memoria:
 * jefe inicial -> cambio obligatorio de clave -> alta de empleados -> permisos por rol ->
 * registro de clientes -> catálogo público. Basada en FlujoUsuarioIntegrationTests de Blockazo.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoSeguridadIntegrationTest {

    private static final String JEFE = "jefe@dulcemayor.test";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    // ------------------------------------------------------------------ auxiliares

    private String login(String correo, String clave) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    private String cambiarClave(String token, String actual, String nueva) throws Exception {
        String cuerpo = mvc.perform(put("/api/auth/clave").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"" + actual + "\",\"nuevaClave\":\"" + nueva
                                + "\",\"confirmarClave\":\"" + nueva + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.debeCambiarClave").value(false))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    private String jefeHabilitado() throws Exception {
        return cambiarClave(login(JEFE, "dulcemayor"), "dulcemayor", "JefeSeguro2026");
    }

    /** Navega Argentina > Mendoza > Capital y devuelve el id de la Ciudad de Mendoza (datos iniciales). */
    private String idCiudadDeMendoza(String token) throws Exception {
        String paises = mvc.perform(get("/api/paises").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String idPais = JsonPath.read(paises, "$[0].id");
        String provincias = mvc.perform(get("/api/provincias/pais/" + idPais).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        String idProvincia = JsonPath.read(provincias, "$[0].id");
        String departamentos = mvc.perform(get("/api/departamentos/provincia/" + idProvincia).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        List<String> idsCapital = JsonPath.read(departamentos, "$[?(@.nombre == 'Capital')].id");
        String localidades = mvc.perform(get("/api/localidades/departamento/" + idsCapital.get(0)).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(localidades, "$[0].id");
    }

    private String empleadoJson(String rol, String correo, String dni, String idLocalidad) {
        return """
                {"nombre":"Ana","apellido":"Pérez","fechaNacimiento":"1990-05-20","tipoDocumento":"DNI",
                 "numeroDocumento":"%s","correo":"%s","rol":"%s","zona":"Zona Este",
                 "vehiculo":"Fiat Fiorino","patente":"AB123CD",
                 "direccion":{"calle":"San Martín","numeracion":"1234","idLocalidad":"%s"}}
                """.formatted(dni, correo, rol, idLocalidad);
    }

    // ------------------------------------------------------------------ pruebas

    @Test
    void elJefeInicialDebeCambiarLaClaveTemporalAntesDeOperar() throws Exception {
        String temporal = login(JEFE, "dulcemayor");

        mvc.perform(get("/api/empleados").header("Authorization", temporal))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Accion-Requerida", "CAMBIAR_CLAVE"));
        mvc.perform(get("/api/auth/yo").header("Authorization", temporal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarClave").value(true));

        String habilitado = cambiarClave(temporal, "dulcemayor", "JefeSeguro2026");

        mvc.perform(get("/api/empleados").header("Authorization", habilitado)).andExpect(status().isOk());
        // La clave quedó guardada con BCrypt, nunca en texto plano
        String hash = repositorioUsuario.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(JEFE).orElseThrow().getClave();
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void elJefeDaDeAltaUnPreventistaQueSoloOperaTrasCambiarSuClave() throws Exception {
        String jefe = jefeHabilitado();
        String idLocalidad = idCiudadDeMendoza(jefe);

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("PREVENTISTA", "ana@dulcemayor.test", "30111222", idLocalidad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PREVENTISTA"))
                .andExpect(jsonPath("$.debeCambiarClave").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());

        String temporal = login("ana@dulcemayor.test", "dulcemayor");
        mvc.perform(get("/api/productos").header("Authorization", temporal)).andExpect(status().isForbidden());

        String preventista = cambiarClave(temporal, "dulcemayor", "Preventa2026");
        mvc.perform(get("/api/productos").header("Authorization", preventista)).andExpect(status().isOk());
        mvc.perform(post("/api/categorias").header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Chicles\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/empleados").header("Authorization", preventista)).andExpect(status().isForbidden());
    }

    @Test
    void elJefeNoPuedeCrearOtroJefeYElAdministrativoGestionaElCatalogo() throws Exception {
        String jefe = jefeHabilitado();
        String idLocalidad = idCiudadDeMendoza(jefe);

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("JEFE", "jefe2@dulcemayor.test", "30111333", idLocalidad)))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("ADMINISTRATIVO", "admin@dulcemayor.test", "30111444", idLocalidad)))
                .andExpect(status().isCreated());
        String administrativo = cambiarClave(login("admin@dulcemayor.test", "dulcemayor"), "dulcemayor", "Almacen2026");

        mvc.perform(post("/api/categorias").header("Authorization", administrativo).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Chocolates\"}")).andExpect(status().isCreated());
        // El jefe hereda los permisos del administrativo
        mvc.perform(post("/api/categorias").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Alfajores\"}")).andExpect(status().isCreated());
        // Pero el administrativo no gestiona empleados
        mvc.perform(get("/api/empleados").header("Authorization", administrativo)).andExpect(status().isForbidden());
    }

    @Test
    void elClienteSeRegistraConSuClaveYNoPuedeElegirSuRol() throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Marta","apellido":"Suárez","correo":"Marta@Kiosco.com","clave":"Golosinas2026",
                                 "confirmacionClave":"Golosinas2026","rol":"JEFE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.debeCambiarClave").value(false))
                .andExpect(jsonPath("$.usuario.correo").value("marta@kiosco.com"))
                .andReturn().getResponse().getContentAsString();
        String cliente = "Bearer " + JsonPath.read(cuerpo, "$.token");

        mvc.perform(get("/api/clientes/yo").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Marta"));
        mvc.perform(get("/api/empleados").header("Authorization", cliente)).andExpect(status().isForbidden());
        mvc.perform(post("/api/categorias").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"X\"}")).andExpect(status().isForbidden());

        // Puede volver a ingresar con su clave, sin cambio obligatorio
        login("marta@kiosco.com", "Golosinas2026");
    }

    @Test
    void sinTokenSoloSeAccedeALoPublico() throws Exception {
        mvc.perform(get("/api/catalogo")).andExpect(status().isOk());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/empleados")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + JEFE + "\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }
}

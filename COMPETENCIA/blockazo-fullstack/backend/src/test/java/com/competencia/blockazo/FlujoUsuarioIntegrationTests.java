package com.competencia.blockazo;

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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de punta a punta: registro -> login -> mi perfil -> modificar perfil
 * -> cambiar clave -> ABM de usuarios (admin). Usa H2 en memoria.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoUsuarioIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    private static final String REGISTRO = """
            {"nombre":"Juan","apellido":"Perez","dni":"30111222","fechaNacimiento":"1995-05-20",
             "direccion":"San Martin 123","correo":"Juan@Mail.com","clave":"secreta1","confirmacionClave":"secreta1"}
            """;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String token(String json) {
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    private String login(String correo, String clave) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return token(body);
    }

    @Test
    void registroLoginPerfilYCambioDeClave() throws Exception {
        // Registro
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.correo").value("juan@mail.com"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());

        // Registro repetido -> error de negocio
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isBadRequest());

        // Login con clave incorrecta -> 401
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"juan@mail.com\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());

        String auth = login("juan@mail.com", "secreta1");

        // Sin token -> 401
        mvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());

        // Mi perfil
        mvc.perform(get("/api/perfil").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("30111222"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-05-20"));

        // Modificar todos los datos (incluido el correo) -> devuelve token nuevo
        String perfilBody = mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan Carlos","apellido":"Perez","dni":"30111223","fechaNacimiento":"1995-05-21",
                                 "direccion":"Belgrano 456","correo":"juancarlos@mail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombre").value("Juan Carlos"))
                .andExpect(jsonPath("$.usuario.correo").value("juancarlos@mail.com"))
                .andReturn().getResponse().getContentAsString();
        String auth2 = token(perfilBody);

        // La clave se conserva tras modificar el perfil
        login("juancarlos@mail.com", "secreta1");

        // Cambiar clave con clave actual incorrecta -> 400
        mvc.perform(put("/api/perfil/clave").header("Authorization", auth2).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"mala123\",\"claveNueva\":\"nueva123\",\"confirmacionClave\":\"nueva123\"}"))
                .andExpect(status().isBadRequest());

        // Cambiar clave OK
        mvc.perform(put("/api/perfil/clave").header("Authorization", auth2).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"secreta1\",\"claveNueva\":\"nueva123\",\"confirmacionClave\":\"nueva123\"}"))
                .andExpect(status().isOk());

        login("juancarlos@mail.com", "nueva123");

        // Un usuario comun no puede usar el ABM
        mvc.perform(get("/api/usuarios").header("Authorization", auth2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void abmDeUsuariosConPatronTemplate() throws Exception {
        String admin = login("admin@blockazo.com", "admin123");

        // Alta
        String creado = mvc.perform(post("/api/usuarios").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellido":"Gomez","dni":"28999888","fechaNacimiento":"1990-01-10",
                                 "direccion":"Mitre 1","correo":"ana@mail.com","clave":"anaana1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(creado, "$.id");

        // Validacion del hook validar(): DNI invalido
        mvc.perform(post("/api/usuarios").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"X","apellido":"Y","dni":"12","fechaNacimiento":"1990-01-10",
                                 "direccion":"Z","correo":"x@mail.com","clave":"xxxxxx"}
                                """))
                .andExpect(status().isBadRequest());

        // Listar / consultar
        mvc.perform(get("/api/usuarios").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/usuarios/" + id).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@mail.com"));

        // Modificacion sin clave -> conserva la anterior
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Maria","apellido":"Gomez","dni":"28999888","fechaNacimiento":"1990-01-10",
                                 "direccion":"Mitre 2","correo":"ana@mail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Maria"));
        login("ana@mail.com", "anaana1");

        // Baja logica -> ya no puede loguearse y no aparece en el listado
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@mail.com\",\"clave\":\"anaana1\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios/" + id).header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginConGoogleRedirigeAlProveedor() throws Exception {
        mvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
}

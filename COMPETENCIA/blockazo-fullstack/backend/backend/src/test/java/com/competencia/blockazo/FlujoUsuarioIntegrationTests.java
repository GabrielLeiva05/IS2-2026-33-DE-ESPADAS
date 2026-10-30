package com.competencia.blockazo;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.PerfilRequest;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.service.UsuarioService;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Autowired
    private UsuarioService usuarioService;

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

        // Nombre con numeros -> 400
        mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan2","apellido":"Perez","dni":"30111222","fechaNacimiento":"1995-05-20",
                                 "direccion":"Belgrano 456","correo":"juan@mail.com"}
                                """))
                .andExpect(status().isBadRequest());

        // El correo, el DNI y la fecha de nacimiento no se pueden modificar -> 400
        mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan","apellido":"Perez","dni":"30111222","fechaNacimiento":"1995-05-20",
                                 "direccion":"Belgrano 456","correo":"otro@mail.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El correo no se puede modificar"));
        mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan","apellido":"Perez","dni":"30111223","fechaNacimiento":"1995-05-20",
                                 "direccion":"Belgrano 456","correo":"juan@mail.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El DNI no se puede modificar"));
        mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan","apellido":"Perez","dni":"30111222","fechaNacimiento":"1995-05-21",
                                 "direccion":"Belgrano 456","correo":"juan@mail.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha de nacimiento no se puede modificar"));

        // Se pueden modificar nombre, apellido y direccion (el correo/DNI/fecha se envian iguales)
        String perfilBody = mvc.perform(put("/api/perfil").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan Carlos","apellido":"Perez","dni":"30111222","fechaNacimiento":"1995-05-20",
                                 "direccion":"Belgrano 456","correo":"juan@mail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombre").value("Juan Carlos"))
                .andExpect(jsonPath("$.usuario.correo").value("juan@mail.com"))
                .andExpect(jsonPath("$.usuario.dni").value("30111222"))
                .andReturn().getResponse().getContentAsString();
        String auth2 = token(perfilBody);

        // La clave se conserva tras modificar el perfil
        login("juan@mail.com", "secreta1");

        // Cambiar clave con clave actual incorrecta -> 400
        mvc.perform(put("/api/perfil/clave").header("Authorization", auth2).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"mala123\",\"claveNueva\":\"nueva123\",\"confirmacionClave\":\"nueva123\"}"))
                .andExpect(status().isBadRequest());

        // Cambiar clave OK
        mvc.perform(put("/api/perfil/clave").header("Authorization", auth2).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"secreta1\",\"claveNueva\":\"nueva123\",\"confirmacionClave\":\"nueva123\"}"))
                .andExpect(status().isOk());

        login("juan@mail.com", "nueva123");

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

        // Validacion del hook validar(): el nombre y el apellido no admiten numeros
        mvc.perform(post("/api/usuarios").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Pedro3","apellido":"Lopez","dni":"27111000","fechaNacimiento":"1990-01-10",
                                 "direccion":"Z","correo":"pedro@mail.com","clave":"pedro123"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/usuarios").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Pedro","apellido":"Lopez9","dni":"27111000","fechaNacimiento":"1990-01-10",
                                 "direccion":"Z","correo":"pedro@mail.com","clave":"pedro123"}
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

        // El ABM tampoco permite modificar correo, DNI ni fecha de nacimiento
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Maria","apellido":"Gomez","dni":"28999889","fechaNacimiento":"1990-01-10",
                                 "direccion":"Mitre 2","correo":"ana@mail.com"}
                                """))
                .andExpect(status().isBadRequest());

        // Un admin no puede darse de baja a si mismo (regla del servicio)
        Number idAdmin = JsonPath.read(mvc.perform(get("/api/perfil").header("Authorization", admin))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(delete("/api/usuarios/" + idAdmin).header("Authorization", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No puede darse de baja a si mismo"));

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
    void usuarioOAuth2NombresValidosYCompletaPerfilUnaSolaVez() throws Exception {
        // Google devuelve un nombre con numeros y no devuelve apellido -> se limpian / se usa un valor por defecto
        Usuario u = usuarioService.registrarOIngresarOAuth2("goog@mail.com", "Pedro3", null, Proveedor.GOOGLE);
        assertEquals("Pedro", u.getNombre());
        assertEquals("Sin apellido", u.getApellido());

        // Puede completar DNI y fecha porque todavia estan vacios
        usuarioService.actualizarPerfil("goog@mail.com", new PerfilRequest("Pedro", "Gomez", "30222333",
                LocalDate.of(1992, 3, 4), "Calle 1", "goog@mail.com"));

        // Una vez cargados, ya no se pueden modificar
        assertThrows(ErrorServiceException.class, () ->
                usuarioService.actualizarPerfil("goog@mail.com", new PerfilRequest("Pedro", "Gomez", "30222334",
                        LocalDate.of(1992, 3, 4), "Calle 1", "goog@mail.com")));
        assertThrows(ErrorServiceException.class, () ->
                usuarioService.actualizarPerfil("goog@mail.com", new PerfilRequest("Pedro", "Gomez", "30222333",
                        LocalDate.of(1992, 3, 5), "Calle 1", "goog@mail.com")));
    }

    @Test
    void loginConGoogleRedirigeAlProveedor() throws Exception {
        mvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
}

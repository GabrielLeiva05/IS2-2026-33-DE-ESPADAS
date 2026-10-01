package com.ejercicioIntegrador.tiendaderopa;

import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.service.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class TiendaderopaApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioServicio usuarioServicio;

	@Test
	void contextLoads() {
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void clienteNoPuedeAccederAProveedores() throws Exception {
		mockMvc.perform(get("/proveedores"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATIVO")
	void administradorNoPuedeAccederAlPerfilDeCliente() throws Exception {
		mockMvc.perform(get("/perfil"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATIVO")
	void administradorNoPuedeAccederALaPaginaDePerfilCliente() throws Exception {
		mockMvc.perform(get("/perfil"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void clienteNoPuedeCambiarElEstadoDeUnaOrden() throws Exception {
		mockMvc.perform(post("/admin/ordenes-compra/orden-1/estado")
					.param("estado", "PAGO_REALIZADO")
					.with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void rutaMvcDeEscrituraRequiereTokenCsrf() throws Exception {
		mockMvc.perform(post("/mis-ordenes/crear"))
				.andExpect(status().isForbidden());
	}


	@Test
	@WithMockUser(roles = "ADMINISTRATIVO")
	void yaNoSePublicaLaApiRestDeClientes() throws Exception {
		mockMvc.perform(get("/api/v1/clientes"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/ordenes-compra"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/perfil"))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATIVO")
	void listadoAdministrativoSeRenderizaComoHtml() throws Exception {
		mockMvc.perform(get("/admin/nacionalidades"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRATIVO")
	void modificarUsuarioDesdeElMenuAdminCargaElFormulario() throws Exception {
		Usuario usuario = usuarioServicio.listarTodos().stream().findFirst().orElseThrow();
		String rutaEdicion = "/admin/usuarios/" + usuario.getId() + "/editar";

		mockMvc.perform(get("/admin/dashboard").sessionAttr("usuariosession", usuario))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(rutaEdicion)));

		mockMvc.perform(get(rutaEdicion).sessionAttr("usuariosession", usuario))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Editar registro")));
	}
}

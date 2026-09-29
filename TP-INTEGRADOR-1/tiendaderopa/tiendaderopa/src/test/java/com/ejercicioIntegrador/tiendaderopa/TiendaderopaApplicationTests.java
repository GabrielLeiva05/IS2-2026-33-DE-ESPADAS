package com.ejercicioIntegrador.tiendaderopa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TiendaderopaApplicationTests {

	@Autowired
	private MockMvc mockMvc;

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
		mockMvc.perform(get("/api/v1/perfil"))
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
		mockMvc.perform(put("/api/ordenes-compra/orden-1/estado")
					.param("estado", "PAGO_REALIZADO")
					.with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void apiDeEscrituraRequiereTokenCsrf() throws Exception {
		mockMvc.perform(post("/api/ordenes-compra"))
				.andExpect(status().isForbidden());
	}

}

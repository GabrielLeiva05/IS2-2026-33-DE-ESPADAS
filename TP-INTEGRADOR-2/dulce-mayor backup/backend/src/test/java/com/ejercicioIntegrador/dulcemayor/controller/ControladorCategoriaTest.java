package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.ManejadorErrores;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica el algoritmo del BaseController (códigos HTTP por caso de uso) y la traducción de errores. */
class ControladorCategoriaTest {

    private ServicioCategoria servicio;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        servicio = mock(ServicioCategoria.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorCategoria(servicio))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void altaDevuelve201ConLaUbicacionDelRecurso() throws Exception {
        when(servicio.alta(any(CategoriaRequestDTO.class)))
                .thenReturn(new CategoriaResponseDTO("cat-1", "Chocolates", false));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Chocolates\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/categorias/cat-1"))
                .andExpect(jsonPath("$.nombre").value("Chocolates"));
    }

    @Test
    void unErrorDeNegocioDevuelve400ConElMensaje() throws Exception {
        when(servicio.alta(any(CategoriaRequestDTO.class)))
                .thenThrow(new MiException("Ya existe una categoría con el nombre Chocolates"));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Chocolates\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una categoría con el nombre Chocolates"));
    }

    @Test
    void consultarUnRecursoInexistenteDevuelve404() throws Exception {
        when(servicio.consultar("no-existe")).thenThrow(new RecursoNoEncontradoException("No existe la categoría"));

        mockMvc.perform(get("/api/categorias/no-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarDevuelveLasCategoriasActivas() throws Exception {
        when(servicio.listarActivos()).thenReturn(List.of(new CategoriaResponseDTO("cat-1", "Chocolates", false)));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"));
    }

    @Test
    void bajaDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/categorias/cat-1"))
                .andExpect(status().isNoContent());

        verify(servicio).baja("cat-1");
    }
}

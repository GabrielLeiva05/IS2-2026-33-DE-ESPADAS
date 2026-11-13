package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.ManejadorErrores;
import com.ejercicioIntegrador.dulcemayor.dto.StockResponseDTO;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica el paso redefinido operacionHabilitada() de ControladorStock. */
class ControladorStockTest {

    private ServicioStock servicio;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        servicio = mock(ServicioStock.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorStock(servicio))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void modificarPorPutNoEstaPermitido() throws Exception {
        mockMvc.perform(put("/api/stock/s-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"prod-1\",\"cantidadInicial\":10,\"stockMinimo\":0}"))
                .andExpect(status().isMethodNotAllowed());

        verify(servicio, never()).modificar(anyString(), any());
    }

    @Test
    void darDeBajaNoEstaPermitido() throws Exception {
        mockMvc.perform(delete("/api/stock/s-1"))
                .andExpect(status().isMethodNotAllowed());

        verify(servicio, never()).baja(anyString());
    }

    @Test
    void reponerDevuelveElStockActualizado() throws Exception {
        when(servicio.reponerStock("prod-1", 30)).thenReturn(new StockResponseDTO("s-1", "prod-1", "ALF-001",
                "Alfajor triple", 30, 10, false, LocalDateTime.now()));

        mockMvc.perform(patch("/api/stock/producto/prod-1/reponer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadActual").value(30));
    }
}

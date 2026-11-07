package com.dulcemayor.institucional;

import com.dulcemayor.institucional.dto.CotizacionDTO;
import com.dulcemayor.institucional.dto.DireccionDTO;
import com.dulcemayor.institucional.dto.SucursalDTO;
import com.dulcemayor.institucional.service.ServicioSitio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza la página pública de sucursales con el motor real. */
@SpringBootTest
class PlantillaSucursalesTest {

    @Autowired
    private WebApplicationContext contexto;

    @MockitoBean
    private ServicioSitio servicioSitio;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void laPaginaPublicaMuestraSucursalesYCotizacion() throws Exception {
        SucursalDTO sucursal = new SucursalDTO();
        sucursal.setNombre("Sucursal Centro");
        sucursal.setHorario("Lunes a viernes de 9 a 18");
        sucursal.setTelefono("2615550000");
        DireccionDTO direccion = new DireccionDTO();
        direccion.setCalle("San Martín");
        direccion.setNumeracion("100");
        direccion.setLocalidad("Ciudad de Mendoza");
        direccion.setLatitud(-32.89);
        direccion.setLongitud(-68.84);
        sucursal.setDireccion(direccion);

        CotizacionDTO cotizacion = new CotizacionDTO();
        cotizacion.setValor(new BigDecimal("1234.50"));
        cotizacion.setActualizada(true);

        when(servicioSitio.sucursales()).thenReturn(List.of(sucursal));
        when(servicioSitio.cotizacion()).thenReturn(cotizacion);

        mvc().perform(get("/sucursales"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sucursal Centro")))
                .andExpect(content().string(containsString("San Martín 100")))
                .andExpect(content().string(containsString("Dólar BCRA")))
                .andExpect(content().string(containsString("OpenStreetMap")));
    }

    @Test
    void sinSucursalesLaPaginaSigueFuncionando() throws Exception {
        when(servicioSitio.sucursales()).thenReturn(List.of());
        when(servicioSitio.cotizacion()).thenReturn(null);

        mvc().perform(get("/sucursales"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay sucursales publicadas")));
    }
}

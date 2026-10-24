package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.MercadoPagoCheckoutService;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TiendaWebControllerTest {

    @Test
    void retornoProcesaCollectionIdCuandoPaymentIdNoEstaPresente() throws Exception {
        MercadoPagoCheckoutService checkoutService = mock(MercadoPagoCheckoutService.class);
        TiendaWebController controller = new TiendaWebController(
                mock(ServicioOrdenCompra.class), checkoutService);
        OrdenCompra orden = new OrdenCompra();
        orden.setEstadoOrdenCompra(EstadoOrdenCompra.PAGO_REALIZADO);
        when(checkoutService.procesarRetorno("orden-1", "pago-1", "cliente@example.com"))
                .thenReturn(orden);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.resultado(
                User.withUsername("cliente@example.com").password("hash").roles("CLIENTE").build(),
                "orden-1", null, "pago-1", null, "approved", model);

        verify(checkoutService).procesarRetorno("orden-1", "pago-1", "cliente@example.com");
        assertEquals("approved", model.getAttribute("estadoInformado"));
        assertEquals("El pago fue aprobado. Tu compra quedó registrada.", model.getAttribute("mensaje"));
    }
}

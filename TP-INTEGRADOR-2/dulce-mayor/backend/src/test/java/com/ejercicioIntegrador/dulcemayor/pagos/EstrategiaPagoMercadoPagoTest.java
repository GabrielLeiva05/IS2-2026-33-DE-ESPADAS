package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstrategiaPagoMercadoPagoTest {

    private ClienteMercadoPago cliente;
    private EstrategiaPagoMercadoPago estrategia;
    private Pago pago;

    @BeforeEach
    void setUp() {
        cliente = mock(ClienteMercadoPago.class);
        estrategia = new EstrategiaPagoMercadoPago(cliente, 24);
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pago = new Pago();
        pago.setId("pag-1");
        pago.setPedido(pedido);
        pago.setMedio(MedioPago.MERCADO_PAGO);
        pago.setImporte(new BigDecimal("150.00"));
        pago.setReferenciaOperacion("ref-op-1");
    }

    @Test
    void iniciarSinCredencialesNoSimulaUnaPreferencia() {
        when(cliente.estaConfigurado()).thenReturn(false);

        MiException error = assertThrows(MiException.class, () -> estrategia.iniciar(pago));
        assertTrue(error.getMessage().contains("no está configurado"));
    }

    @Test
    void iniciarDevuelveElEnlaceYLaReferenciaDeLaPreferencia() throws MiException {
        when(cliente.estaConfigurado()).thenReturn(true);
        when(cliente.crearPreferencia(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(new ClienteMercadoPago.Preferencia("pref-9", "https://mp.test/checkout/pref-9"));

        DatosInicioPago datos = estrategia.iniciar(pago);

        assertEquals("pref-9", datos.referenciaExterna());
        assertEquals("https://mp.test/checkout/pref-9", datos.enlacePago());
        assertNotNull(datos.instrucciones());
        assertEquals(MedioPago.MERCADO_PAGO, estrategia.medio());
    }

    @Test
    void acreditarExigeVerificacionServerToServerAprobada() throws MiException {
        when(cliente.consultarPago("pago-1"))
                .thenReturn(new ClienteMercadoPago.PagoExterno("pago-1", "approved", "ref-op-1"));
        estrategia.acreditar(pago, "pago-1", null);

        when(cliente.consultarPago("pago-2"))
                .thenReturn(new ClienteMercadoPago.PagoExterno("pago-2", "pending", "ref-op-1"));
        MiException pendiente = assertThrows(MiException.class, () -> estrategia.acreditar(pago, "pago-2", null));
        assertTrue(pendiente.getMessage().contains("pending"));

        when(cliente.consultarPago("pago-3"))
                .thenReturn(new ClienteMercadoPago.PagoExterno("pago-3", "approved", "otra-referencia"));
        assertThrows(MiException.class, () -> estrategia.acreditar(pago, "pago-3", null));

        assertThrows(MiException.class, () -> estrategia.acreditar(pago, null, null));
    }

    @Test
    void resolverExigeWebhookConfiguradoYFirmaValida() throws MiException {
        when(cliente.webhookConfigurado()).thenReturn(false);
        assertThrows(MiException.class, () -> estrategia.resolver("firma", "req", "123", "payment"));

        when(cliente.webhookConfigurado()).thenReturn(true);
        when(cliente.firmaValida("firma", "req", "123")).thenReturn(false);
        assertThrows(MiException.class, () -> estrategia.resolver("firma", "req", "123", "payment"));
    }

    @Test
    void resolverIgnoraAvisosQueNoSonDePago() throws MiException {
        when(cliente.webhookConfigurado()).thenReturn(true);
        when(cliente.firmaValida("firma", "req", "123")).thenReturn(true);

        assertNull(estrategia.resolver("firma", "req", "123", "merchant_order"));
    }

    @Test
    void resolverConsultaElPagoYLoInforma() throws MiException {
        when(cliente.webhookConfigurado()).thenReturn(true);
        when(cliente.firmaValida("firma", "req", "777")).thenReturn(true);
        when(cliente.consultarPago("777"))
                .thenReturn(new ClienteMercadoPago.PagoExterno("777", "approved", "ref-op-1"));

        NotificacionPago notificacion = estrategia.resolver("firma", "req", "777", "payment");

        assertEquals("ref-op-1", notificacion.referenciaOperacion());
        assertEquals("777", notificacion.referenciaExterna());
        assertTrue(notificacion.acreditado());
    }
}

package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.pagos.EstrategiaNotificable;
import com.ejercicioIntegrador.dulcemayor.pagos.EstrategiaPagoEfectivo;
import com.ejercicioIntegrador.dulcemayor.pagos.EstrategiaPagoTransferencia;
import com.ejercicioIntegrador.dulcemayor.pagos.NotificacionPago;
import com.ejercicioIntegrador.dulcemayor.pagos.ResolvedorEstrategiaPago;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioPagoTest {

    private RepositorioPago repositorioPago;
    private ResolvedorEstrategiaPago estrategias;
    private ServicioPedido servicioPedido;
    private ServicioComprobante servicioComprobante;
    private ServicioPago servicio;

    private Pedido pedido;

    @BeforeEach
    void setUp() throws MiException {
        repositorioPago = mock(RepositorioPago.class);
        estrategias = mock(ResolvedorEstrategiaPago.class);
        servicioPedido = mock(ServicioPedido.class);
        servicioComprobante = mock(ServicioComprobante.class);
        servicio = new ServicioPago(repositorioPago, estrategias, servicioPedido, servicioComprobante, mock(org.springframework.context.ApplicationEventPublisher.class));

        Preventista preventista = new Preventista();
        preventista.setId("prev-1");
        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setId("pv-1");
        puntoVenta.setNombreComercial("Kiosco Norte");
        puntoVenta.setPreventista(preventista);

        pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setPuntoVenta(puntoVenta);
        pedido.setPreventista(preventista);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setTotal(new BigDecimal("200.00"));

        when(estrategias.obtener(MedioPago.TRANSFERENCIA))
                .thenReturn(new EstrategiaPagoTransferencia("Transferí el total", 24));
        when(estrategias.obtener(MedioPago.EFECTIVO))
                .thenReturn(new EstrategiaPagoEfectivo("Se cobra al entregar", 168));
        when(repositorioPago.save(any(Pago.class))).thenAnswer(invocacion -> {
            Pago guardado = invocacion.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId("pag-1");
            }
            return guardado;
        });
    }

    @Test
    void iniciarCreaUnPagoPendientePorElTotalConVencimiento() throws MiException {
        when(servicioPedido.obtenerPedidoPropio("ped-1", "preventista@test")).thenReturn(pedido);

        PagoResponseDTO creado = servicio.iniciarPorPreventista("ped-1",
                new PagoRequestDTO(MedioPago.TRANSFERENCIA), "preventista@test");

        assertEquals(EstadoPago.PENDIENTE, creado.estado());
        assertEquals(new BigDecimal("200.00"), creado.importe());
        assertEquals(MedioPago.TRANSFERENCIA, creado.medio());
        assertEquals("ped-1", creado.idPedido());
        assertNotNull(creado.instrucciones());
        assertNotNull(creado.referenciaOperacion());
        assertNotNull(creado.fechaVencimiento());
        assertTrue(creado.fechaVencimiento().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void iniciarRechazaUnPedidoQueNoEstaConfirmado() throws MiException {
        pedido.setEstado(EstadoPedido.BORRADOR);
        when(servicioPedido.obtenerPedidoPropio("ped-1", "preventista@test")).thenReturn(pedido);

        assertThrows(MiException.class, () -> servicio.iniciarPorPreventista("ped-1",
                new PagoRequestDTO(MedioPago.TRANSFERENCIA), "preventista@test"));
        verify(repositorioPago, never()).save(any(Pago.class));
    }

    @Test
    void iniciarRechazaUnPedidoYaAcreditado() throws MiException {
        when(servicioPedido.obtenerPedidoPropio("ped-1", "preventista@test")).thenReturn(pedido);
        when(repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse("ped-1", EstadoPago.ACREDITADO))
                .thenReturn(Optional.of(pago(EstadoPago.ACREDITADO, null)));

        assertThrows(MiException.class, () -> servicio.iniciarPorPreventista("ped-1",
                new PagoRequestDTO(MedioPago.TRANSFERENCIA), "preventista@test"));
    }

    @Test
    void iniciarRechazaUnSegundoIntentoPendienteVigente() throws MiException {
        when(servicioPedido.obtenerPedidoPropio("ped-1", "preventista@test")).thenReturn(pedido);
        when(repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse("ped-1", EstadoPago.PENDIENTE))
                .thenReturn(Optional.of(pago(EstadoPago.PENDIENTE, LocalDateTime.now().plusHours(3))));

        assertThrows(MiException.class, () -> servicio.iniciarPorPreventista("ped-1",
                new PagoRequestDTO(MedioPago.TRANSFERENCIA), "preventista@test"));
    }

    @Test
    void iniciarRechazaUnMedioVacio() throws MiException {
        when(servicioPedido.obtenerPedidoPropio("ped-1", "preventista@test")).thenReturn(pedido);

        assertThrows(MiException.class, () -> servicio.iniciarPorPreventista("ped-1", null, "preventista@test"));
        assertThrows(MiException.class, () -> servicio.iniciarPorPreventista("ped-1",
                new PagoRequestDTO(null), "preventista@test"));
    }

    @Test
    void acreditarConsumeStockYEmiteElComprobante() throws MiException {
        Pago pago = this.pago(EstadoPago.PENDIENTE, LocalDateTime.now().plusHours(2));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        PagoResponseDTO acreditado = servicio.acreditar("pag-1", new ConfirmarPagoRequestDTO("TR-77", "ok"));

        assertEquals(EstadoPago.ACREDITADO, acreditado.estado());
        assertNotNull(acreditado.fechaAcreditacion());
        assertEquals("TR-77", acreditado.referenciaExterna());
        verify(servicioPedido).consumirReservasPorPago("ped-1");
        verify(servicioComprobante).emitir(pago);
    }

    @Test
    void acreditarEsIdempotenteYNoVuelveAConsumir() throws MiException {
        Pago pago = this.pago(EstadoPago.ACREDITADO, LocalDateTime.now().plusHours(2));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        PagoResponseDTO resultado = servicio.acreditar("pag-1", new ConfirmarPagoRequestDTO("TR-77", null));

        assertEquals(EstadoPago.ACREDITADO, resultado.estado());
        verify(servicioPedido, never()).consumirReservasPorPago(anyString());
        verify(servicioComprobante, never()).emitir(any(Pago.class));
    }

    @Test
    void acreditarUnPagoVencidoLoExpiraYLiberaLaReserva() throws MiException {
        Pago pago = this.pago(EstadoPago.PENDIENTE, LocalDateTime.now().minusMinutes(1));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        assertThrows(MiException.class,
                () -> servicio.acreditar("pag-1", new ConfirmarPagoRequestDTO("TR-77", null)));

        assertEquals(EstadoPago.EXPIRADO, pago.getEstado());
        verify(servicioPedido).cancelarPorVencimientoDePago("ped-1");
        verify(servicioPedido, never()).consumirReservasPorPago(anyString());
    }

    @Test
    void acreditarRechazaUnPagoQueYaNoEstaPendiente() throws MiException {
        Pago pago = this.pago(EstadoPago.RECHAZADO, null);
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        assertThrows(MiException.class,
                () -> servicio.acreditar("pag-1", new ConfirmarPagoRequestDTO("TR-77", null)));
    }

    @Test
    void rechazarDejaElPagoRechazadoConObservacion() throws MiException {
        Pago pago = this.pago(EstadoPago.PENDIENTE, LocalDateTime.now().plusHours(2));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        PagoResponseDTO rechazado = servicio.rechazar("pag-1",
                new ConfirmarPagoRequestDTO(null, "La transferencia no llegó"));

        assertEquals(EstadoPago.RECHAZADO, rechazado.estado());
        assertEquals("La transferencia no llegó", rechazado.observacion());
        verify(servicioPedido, never()).consumirReservasPorPago(anyString());
    }

    @Test
    void registrarEfectivoCreaYAutoacreditaElPago() throws MiException {
        when(servicioPedido.obtenerPedido("ped-1")).thenReturn(pedido);
        Pago efectivo = pago(EstadoPago.PENDIENTE, LocalDateTime.now().plusDays(7));
        efectivo.setMedio(MedioPago.EFECTIVO);
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(efectivo));

        PagoResponseDTO resultado = servicio.registrarEfectivo("ped-1");

        assertEquals(MedioPago.EFECTIVO, resultado.medio());
        assertEquals(EstadoPago.ACREDITADO, resultado.estado());
        verify(servicioPedido).consumirReservasPorPago("ped-1");
    }

    @Test
    void expirarNoHaceNadaSiElPagoNoVencio() throws MiException {
        Pago pago = this.pago(EstadoPago.PENDIENTE, LocalDateTime.now().plusHours(5));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        assertFalse(servicio.expirar("pag-1"));
        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        assertNull(pago.getFechaAcreditacion());
    }

    @Test
    void expirarLiberaLaReservaDeUnPagoVencido() throws MiException {
        Pago pago = this.pago(EstadoPago.PENDIENTE, LocalDateTime.now().minusHours(2));
        when(repositorioPago.buscarParaActualizar("pag-1")).thenReturn(Optional.of(pago));

        assertTrue(servicio.expirar("pag-1"));

        assertEquals(EstadoPago.EXPIRADO, pago.getEstado());
        verify(servicioPedido).cancelarPorVencimientoDePago("ped-1");
    }

    @Test
    void listarPorPedidoDevuelveLosIntentosDelMasRecienteAlMasAntiguo() {
        when(repositorioPago.findByPedido_IdAndEliminadoFalseOrderByFechaCreacionDesc("ped-1"))
                .thenReturn(List.of(pago(EstadoPago.ACREDITADO, null)));

        List<PagoResponseDTO> pagos = servicio.listarPorPedido("ped-1");

        assertEquals(1, pagos.size());
        assertEquals(EstadoPago.ACREDITADO, pagos.get(0).estado());
    }

    @Test
    void procesarNotificacionIgnoraAvisosQueNoSonDePago() throws MiException {
        when(estrategias.notificable(MedioPago.MERCADO_PAGO)).thenReturn(new EstrategiaNotificable() {
            @Override
            public MedioPago medio() {
                return MedioPago.MERCADO_PAGO;
            }

            @Override
            public NotificacionPago resolver(String firma, String requestId, String dataId, String tipo) {
                return null;
            }
        });

        assertTrue(servicio.procesarNotificacion("firma", "req", "123", "merchant_order").isEmpty());
    }

    private Pago pago(EstadoPago estado, LocalDateTime vencimiento) {
        Pago pago = new Pago();
        pago.setId("pag-1");
        pago.setPedido(pedido);
        pago.setMedio(MedioPago.TRANSFERENCIA);
        pago.setEstado(estado);
        pago.setImporte(new BigDecimal("200.00"));
        pago.setReferenciaOperacion("ref-op-1");
        pago.setFechaVencimiento(vencimiento);
        return pago;
    }
}

package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DescuentoPromocionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPromocion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioPromocionTest {

    private RepositorioPromocion repositorioPromocion;
    private RepositorioPedido repositorioPedido;
    private ServicioPromocion servicio;

    @BeforeEach
    void setUp() {
        repositorioPromocion = mock(RepositorioPromocion.class);
        repositorioPedido = mock(RepositorioPedido.class);
        servicio = new ServicioPromocion(repositorioPromocion, repositorioPedido);
        when(repositorioPromocion.save(any(Promocion.class))).thenAnswer(invocacion -> {
            Promocion guardada = invocacion.getArgument(0);
            if (guardada.getId() == null) {
                guardada.setId("promo-1");
            }
            return guardada;
        });
    }

    @Test
    void unPorcentajeSeCalculaEnElServidorConDosDecimales() throws MiException {
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse(anyString()))
                .thenReturn(Optional.of(promocion("VERANO10", TipoPromocion.PORCENTAJE, "10")));

        DescuentoPromocionDTO descuento = servicio.calcular("verano10", new BigDecimal("333.33"), null);

        assertEquals(new BigDecimal("33.33"), descuento.descuento());
        assertEquals(new BigDecimal("300.00"), descuento.total());
        assertEquals("VERANO10", descuento.codigo());
    }

    @Test
    void unMontoFijoNoPuedeSuperarElSubtotal() throws MiException {
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("GRANDE"))
                .thenReturn(Optional.of(promocion("GRANDE", TipoPromocion.MONTO_FIJO, "5000")));

        DescuentoPromocionDTO descuento = servicio.calcular("GRANDE", new BigDecimal("1200.00"), null);

        assertEquals(new BigDecimal("1200.00"), descuento.descuento());
        assertEquals(new BigDecimal("0.00"), descuento.total());
    }

    @Test
    void rechazaCodigosInexistentesOVencidos() {
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse(anyString())).thenReturn(Optional.empty());
        assertThrows(MiException.class, () -> servicio.calcular("NADA", new BigDecimal("100.00"), null));

        Promocion vencida = promocion("VIEJA", TipoPromocion.PORCENTAJE, "10");
        vencida.setFechaHasta(LocalDate.now().minusDays(1));
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("VIEJA"))
                .thenReturn(Optional.of(vencida));
        assertThrows(MiException.class, () -> servicio.calcular("VIEJA", new BigDecimal("100.00"), null));

        Promocion inactiva = promocion("APAGADA", TipoPromocion.PORCENTAJE, "10");
        inactiva.setActiva(false);
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("APAGADA"))
                .thenReturn(Optional.of(inactiva));
        assertThrows(MiException.class, () -> servicio.calcular("APAGADA", new BigDecimal("100.00"), null));
    }

    @Test
    void rechazaUnaCompraPorDebajoDelMinimo() {
        Promocion conMinimo = promocion("MINIMO", TipoPromocion.PORCENTAJE, "10");
        conMinimo.setMinimoCompra(new BigDecimal("5000"));
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("MINIMO"))
                .thenReturn(Optional.of(conMinimo));

        assertThrows(MiException.class, () -> servicio.calcular("MINIMO", new BigDecimal("1000.00"), null));
    }

    @Test
    void unCodigoDeUsoUnicoNoSeRepiteEnOtroPedido() {
        Promocion unica = promocion("UNICA", TipoPromocion.MONTO_FIJO, "100");
        unica.setUsoUnico(true);
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("UNICA"))
                .thenReturn(Optional.of(unica));
        when(repositorioPedido.countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalseAndIdNot(
                eq("UNICA"), anyCollection(), eq("ped-2"))).thenReturn(1L);

        assertThrows(MiException.class, () -> servicio.calcular("UNICA", new BigDecimal("1000.00"), "ped-2"));
    }

    @Test
    void elMismoPedidoPuedeRevalidarSuCodigo() throws MiException {
        Promocion unica = promocion("UNICA", TipoPromocion.MONTO_FIJO, "100");
        unica.setUsoUnico(true);
        when(repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse("UNICA"))
                .thenReturn(Optional.of(unica));
        when(repositorioPedido.countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalseAndIdNot(
                eq("UNICA"), anyCollection(), eq("ped-1"))).thenReturn(0L);

        assertEquals(new BigDecimal("100.00"), servicio.calcular("UNICA", new BigDecimal("1000.00"), "ped-1").descuento());
    }

    @Test
    void elAltaNormalizaElCodigoYValidaLosDatos() throws MiException {
        when(repositorioPromocion.existsByCodigoIgnoreCaseAndEliminadoFalse("OTOÑO")).thenReturn(false);

        PromocionResponseDTO creada = servicio.alta(new PromocionRequestDTO("  otoño ", "Descuento de otoño",
                TipoPromocion.PORCENTAJE, new BigDecimal("15"), LocalDate.now().minusDays(1), null, true, false, null));

        assertEquals("OTOÑO", creada.codigo());
        assertTrue(creada.vigente());
        assertThrows(MiException.class, () -> servicio.alta(new PromocionRequestDTO("X", "Sin valor",
                TipoPromocion.PORCENTAJE, BigDecimal.ZERO, LocalDate.now(), null, true, false, null)));
        assertThrows(MiException.class, () -> servicio.alta(new PromocionRequestDTO("", "Sin código",
                TipoPromocion.MONTO_FIJO, BigDecimal.TEN, LocalDate.now(), null, true, false, null)));
    }

    @Test
    void elAltaRechazaUnCodigoDuplicado() {
        when(repositorioPromocion.existsByCodigoIgnoreCaseAndEliminadoFalse("VERANO10")).thenReturn(true);

        assertThrows(MiException.class, () -> servicio.alta(new PromocionRequestDTO("verano10", "Repetido",
                TipoPromocion.PORCENTAJE, BigDecimal.TEN, LocalDate.now(), null, true, false, null)));
        verify(repositorioPromocion, never()).save(any(Promocion.class));
    }

    @Test
    void elAltaRechazaUnPorcentajeMayorAlCien() {
        assertThrows(MiException.class, () -> servicio.alta(new PromocionRequestDTO("EXAGERADO", "Imposible",
                TipoPromocion.PORCENTAJE, new BigDecimal("150"), LocalDate.now(), null, true, false, null)));
    }

    @Test
    void laBajaDesactivaYNoBorraFisicamente() throws MiException {
        Promocion promo = promocion("VERANO10", TipoPromocion.PORCENTAJE, "10");
        when(repositorioPromocion.findByIdAndEliminadoFalse("promo-1")).thenReturn(Optional.of(promo));

        servicio.baja("promo-1");

        assertTrue(promo.isEliminado());
        assertFalse(promo.isActiva());
        verify(repositorioPromocion).save(promo);
    }

    @Test
    void listarDevuelveLasPromocionesVigentesEInactivas() {
        when(repositorioPromocion.findByEliminadoFalse()).thenReturn(List.of(
                promocion("A", TipoPromocion.PORCENTAJE, "10"),
                promocion("B", TipoPromocion.MONTO_FIJO, "50")));

        List<PromocionResponseDTO> promociones = servicio.listar();

        assertEquals(2, promociones.size());
    }

    private Promocion promocion(String codigo, TipoPromocion tipo, String valor) {
        Promocion promocion = new Promocion();
        promocion.setId("promo-" + codigo);
        promocion.setCodigo(codigo);
        promocion.setDescripcion("Promo " + codigo);
        promocion.setTipo(tipo);
        promocion.setValor(new BigDecimal(valor));
        promocion.setFechaDesde(LocalDate.now().minusDays(10));
        promocion.setActiva(true);
        return promocion;
    }
}

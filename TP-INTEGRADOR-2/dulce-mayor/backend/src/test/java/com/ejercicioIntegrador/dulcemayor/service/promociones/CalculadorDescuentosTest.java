package com.ejercicioIntegrador.dulcemayor.service.promociones;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.CodigoDescuento;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.PromocionPorcentual;
import com.ejercicioIntegrador.dulcemayor.model.PromocionVolumen;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.model.TramoVolumen;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de descuento: porcentual por producto, por volumen, códigos y su combinación (BigDecimal). */
class CalculadorDescuentosTest {

    private static final SubCategoria ALFAJORES = sub("s-alf");
    private static final SubCategoria CARAMELOS = sub("s-car");
    private static final Producto ALFAJOR = producto("p-alf", ALFAJORES);
    private static final Producto CARAMELO = producto("p-car", CARAMELOS);

    private static SubCategoria sub(String id) {
        SubCategoria s = new SubCategoria();
        s.setId(id);
        return s;
    }

    private static Producto producto(String id, SubCategoria sub) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(id);
        p.setSubCategoria(sub);
        return p;
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private static RenglonDescuento renglon(Producto producto, int cantidad, String precio) {
        return new RenglonDescuento(producto, cantidad, bd(precio));
    }

    private static PromocionPorcentual porcentual(String porcentaje, Producto producto) {
        PromocionPorcentual p = new PromocionPorcentual();
        p.setId("pp-" + porcentaje);
        p.setNombre(porcentaje + " %");
        p.setPorcentaje(bd(porcentaje));
        p.getProductos().add(producto);
        return p;
    }

    /** 1–9 normal, 10–19 5 %, 20+ 10 % sobre una subcategoría. */
    private static PromocionVolumen volumen(SubCategoria sub) {
        PromocionVolumen v = new PromocionVolumen();
        v.setId("pv");
        v.setNombre("Volumen");
        v.getSubCategorias().add(sub);
        v.reemplazarTramos(List.of(new TramoVolumen(10, 19, bd("5")), new TramoVolumen(20, null, bd("10"))));
        return v;
    }

    private static CodigoDescuento codigo(TipoValorDescuento tipo, String valor) {
        CodigoDescuento c = new CodigoDescuento();
        c.setId("c-1");
        c.setNombre("Dulce");
        c.setCodigo("DULCE10");
        c.setTipoValor(tipo);
        c.setValor(bd(valor));
        c.setFechaInicio(LocalDate.now().minusDays(1));
        c.setFechaFin(LocalDate.now().plusDays(1));
        return c;
    }

    @Test
    void sinPromocionesElTotalEsElSubtotal() throws MiException {
        ResultadoDescuentos r = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 3, "1000.10")), List.of(), null);

        assertEquals(bd("3000.30"), r.subtotal());
        assertEquals(bd("0.00"), r.totalDescuento());
        assertEquals(bd("3000.30"), r.total());
        assertTrue(r.descuentos().isEmpty());
    }

    @Test
    void descuentoPorcentualSoloSobreElProductoAlcanzado() throws MiException {
        PromocionPorcentual quince = porcentual("15", ALFAJOR);
        ResultadoDescuentos r = CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 2, "1000"), renglon(CARAMELO, 1, "500")), List.of(quince), null);

        assertEquals(bd("2500.00"), r.subtotal());
        assertEquals(bd("300.00"), r.totalDescuento());              // 15 % de 2000
        assertEquals(bd("2200.00"), r.total());
        assertEquals(bd("300.00"), r.descuentoDelRenglon(0));
        assertEquals(BigDecimal.ZERO, r.descuentoDelRenglon(1));
    }

    @Test
    void descuentoPorVolumenSegunLosTramos() throws MiException {
        PromocionVolumen v = volumen(ALFAJORES);
        assertEquals(bd("0.00"), CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 9, "100")), List.of(v), null).totalDescuento());
        assertEquals(bd("50.00"), CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 10, "100")), List.of(v), null).totalDescuento());
        assertEquals(bd("95.00"), CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 19, "100")), List.of(v), null).totalDescuento());
        assertEquals(bd("200.00"), CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 20, "100")), List.of(v), null).totalDescuento());
        assertEquals(bd("0.00"), CalculadorDescuentos.calcular(List.of(renglon(CARAMELO, 50, "100")), List.of(v), null).totalDescuento());
    }

    @Test
    void lasPromocionesAutomaticasNoSeSumanSeAplicaLaMejor() throws MiException {
        PromocionPorcentual siete = porcentual("7", ALFAJOR);
        PromocionVolumen v = volumen(ALFAJORES);

        ResultadoDescuentos con12 = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 12, "100")), List.of(siete, v), null);
        ResultadoDescuentos con25 = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 25, "100")), List.of(siete, v), null);

        assertEquals(bd("84.00"), con12.totalDescuento());           // 7 % > 5 % del tramo
        assertSame(siete, con12.descuentos().get(0).promocion());
        assertEquals(bd("250.00"), con25.totalDescuento());          // 10 % del tramo > 7 %
        assertSame(v, con25.descuentos().get(0).promocion());
        assertEquals(1, con25.descuentos().size());
    }

    @Test
    void elCodigoPorcentualSeAplicaSobreLoQueQuedoDespuesDeLaPromocion() throws MiException {
        CodigoDescuento diez = codigo(TipoValorDescuento.PORCENTAJE, "10");
        ResultadoDescuentos r = CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 2, "1000"), renglon(CARAMELO, 1, "500")), List.of(porcentual("15", ALFAJOR)), diez);

        // alfajor: 2000 − 300 (15 %) = 1700 − 170 (10 %); caramelo: 500 − 50
        assertEquals(bd("520.00"), r.totalDescuento());
        assertEquals(bd("1980.00"), r.total());
        assertEquals(3, r.descuentos().size());
        assertSame(diez, r.codigo());
    }

    @Test
    void unCodigoNoAcumulableNoSeAplicaSobreRenglonesConPromocion() throws MiException {
        CodigoDescuento diez = codigo(TipoValorDescuento.PORCENTAJE, "10");
        diez.setAcumulable(false);
        ResultadoDescuentos r = CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 2, "1000"), renglon(CARAMELO, 1, "500")), List.of(porcentual("15", ALFAJOR)), diez);

        assertEquals(bd("350.00"), r.totalDescuento());              // 300 de la promoción + 50 del código (caramelo)

        CodigoDescuento soloAlfajor = codigo(TipoValorDescuento.PORCENTAJE, "10");
        soloAlfajor.setAcumulable(false);
        soloAlfajor.getProductos().add(ALFAJOR);
        assertThrows(MiException.class, () -> CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 2, "1000")), List.of(porcentual("15", ALFAJOR)), soloAlfajor));
    }

    @Test
    void codigoDeImporteFijoNoSuperaLoAlcanzado() throws MiException {
        CodigoDescuento quinientos = codigo(TipoValorDescuento.IMPORTE_FIJO, "500");
        ResultadoDescuentos r = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 2, "1000")), List.of(), quinientos);
        assertEquals(bd("500.00"), r.totalDescuento());
        assertEquals(DescuentoCalculado.PEDIDO_COMPLETO, r.descuentos().get(0).indiceRenglon());

        CodigoDescuento enorme = codigo(TipoValorDescuento.IMPORTE_FIJO, "999999");
        ResultadoDescuentos tope = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 1, "300")), List.of(), enorme);
        assertEquals(bd("300.00"), tope.totalDescuento());
        assertEquals(bd("0.00"), tope.total());
    }

    @Test
    void elCodigoExigeElMontoMinimoYProductosAlcanzados() {
        CodigoDescuento conMinimo = codigo(TipoValorDescuento.PORCENTAJE, "10");
        conMinimo.setMontoMinimo(bd("5000"));
        MiException minimo = assertThrows(MiException.class, () -> CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 4, "1000")), List.of(), conMinimo));
        assertTrue(minimo.getMessage().contains("compra mínima"));

        CodigoDescuento soloCaramelos = codigo(TipoValorDescuento.PORCENTAJE, "10");
        soloCaramelos.getSubCategorias().add(CARAMELOS);
        assertThrows(MiException.class, () -> CalculadorDescuentos.calcular(
                List.of(renglon(ALFAJOR, 4, "1000")), List.of(), soloCaramelos));
    }

    @Test
    void elRedondeoEsACentavosSinErroresDeDouble() throws MiException {
        // 3 × 33,33 = 99,99; 15 % = 14,9985 → 15,00
        ResultadoDescuentos r = CalculadorDescuentos.calcular(List.of(renglon(ALFAJOR, 3, "33.33")),
                List.of(porcentual("15", ALFAJOR)), null);
        assertEquals(bd("15.00"), r.totalDescuento());
        assertEquals(bd("84.99"), r.total());
    }
}

package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuscadorProductosTest {

    static ProductoCatalogoDTO producto(String id, String codigo, String nombre, String categoria, String sub, double precio) {
        return new ProductoCatalogoDTO(id, codigo, nombre, "", Presentacion.CAJA, 12, "c", categoria, "s", sub, false,
                precio, 10, List.of(), null, null, null, List.of());
    }

    private static final List<ProductoCatalogoDTO> CATALOGO = List.of(
            producto("1", "ALF-MAI", "Alfajor de maicena", "Golosinas", "Alfajores", 2000),
            producto("2", "ALF-CHO", "Alfajor de chocolate", "Golosinas", "Alfajores", 3000),
            producto("3", "CHI-MEN", "Chicles de menta", "Golosinas", "Chicles", 800),
            producto("4", "TUR-MAN", "Turrón de maní", "Golosinas", "Turrones", 500));

    @Test
    void encuentraSinTildesNiPluralesYCuandoElNombreEsCompleto() {
        assertEquals("1", BuscadorProductos.buscar("alfajores de maicena", CATALOGO).unico().id());
        assertEquals("4", BuscadorProductos.buscar("TURRONES DE MANI", CATALOGO).unico().id());
        assertEquals("3", BuscadorProductos.buscar("chicle", CATALOGO).unico().id());
    }

    @Test
    void elCodigoExactoGana() {
        assertEquals("2", BuscadorProductos.buscar("alf-cho", CATALOGO).unico().id());
    }

    @Test
    void siVariosEmpatanEsAmbiguo() {
        BuscadorProductos.Resultado resultado = BuscadorProductos.buscar("alfajores", CATALOGO);
        assertTrue(resultado.ambiguo());
        assertEquals(List.of("2", "1"), resultado.candidatos().stream().map(ProductoCatalogoDTO::id).toList());
    }

    @Test
    void loQueNoEstaEnElCatalogoNoSeInventa() {
        BuscadorProductos.Resultado resultado = BuscadorProductos.buscar("helado de frutilla", CATALOGO);
        assertFalse(resultado.encontrado());
        assertFalse(resultado.ambiguo());
        assertTrue(BuscadorProductos.buscar("   ", CATALOGO).candidatos().isEmpty());
    }

    @Test
    void filtraPorRubro() {
        assertEquals(List.of("4"), BuscadorProductos.delRubro("turrones", CATALOGO).stream().map(ProductoCatalogoDTO::id).toList());
        assertEquals(4, BuscadorProductos.delRubro("golosinas", CATALOGO).size());
    }
}

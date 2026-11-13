package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Lectura de respuestas de Gemini (sin red: se prueban los cuerpos JSON tal como los devuelve la API). */
class ProveedorIAGeminiTest {

    private static String respuesta(String json) {
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":" + com.fasterxml.jackson.databind.node.TextNode.valueOf(json)
                + "}],\"role\":\"model\"},\"finishReason\":\"STOP\"}]}";
    }

    @Test
    void leeAccionItemsYMonto() throws Exception {
        InterpretacionIA i = ProveedorIAGemini.leer(respuesta(
                "{\"accion\":\"AGREGAR\",\"items\":[{\"producto\":\"alfajores de maicena\",\"cantidad\":5},{\"producto\":\"chicles\"}],"
                        + "\"presupuesto\":null,\"categoria\":null,\"respuesta\":\"Dale\"}"));
        assertEquals(AccionPresupuesto.AGREGAR, i.accion());
        assertEquals(2, i.items().size());
        assertEquals(5, i.items().get(0).cantidad());
        assertNull(i.items().get(1).cantidad());
        assertNull(i.presupuesto());

        InterpretacionIA r = ProveedorIAGemini.leer(respuesta(
                "{\"accion\":\"RECOMENDAR\",\"items\":[],\"presupuesto\":50000,\"categoria\":\"chocolates\",\"respuesta\":\"\"}"));
        assertEquals(0, new BigDecimal("50000").compareTo(r.presupuesto()));
        assertEquals("chocolates", r.categoria());
    }

    @Test
    void unaAccionDesconocidaSeTomaComoOtra() throws Exception {
        assertEquals(AccionPresupuesto.OTRA, ProveedorIAGemini.leer(respuesta("{\"accion\":\"COMPRAR\",\"items\":[],\"respuesta\":\"\"}")).accion());
    }

    @Test
    void respuestasInvalidasSonError() {
        assertThrows(ErrorProveedorIA.class, () -> ProveedorIAGemini.leer("no es json"));
        assertThrows(ErrorProveedorIA.class, () -> ProveedorIAGemini.leer("{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}"));
        assertThrows(ErrorProveedorIA.class, () -> ProveedorIAGemini.leer(respuesta("texto suelto")));
        assertThrows(ErrorProveedorIA.class, () -> ProveedorIAGemini.leer(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void laSolicitudPideJsonConEsquemaYNoLlevaLaClave() {
        ProveedorIAGemini gemini = new ProveedorIAGemini("https://ejemplo", "modelo", "clave-de-prueba");
        assertTrue(gemini.configurado());
        assertFalse(new ProveedorIAGemini("https://ejemplo", "modelo", " ").configurado());
        Map<String, Object> cuerpo = gemini.solicitud(List.of(new MensajeChat(true, "hola"), new MensajeChat(false, "¿qué necesitás?")),
                "5 alfajores", "vacío");
        assertEquals(3, ((List<?>) cuerpo.get("contents")).size());
        Map<String, Object> config = (Map<String, Object>) cuerpo.get("generationConfig");
        assertEquals("application/json", config.get("responseMimeType"));
        assertFalse(cuerpo.toString().contains("clave-de-prueba"));
    }
}

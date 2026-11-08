package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Ola 1: el documento no se puede modificar una vez registrado (también desde administración). */
class ValidacionesPersonaTest {

    private static Cliente conDocumento() {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(TipoDocumento.DNI);
        cliente.setNumeroDocumento("35111222");
        return cliente;
    }

    @Test
    void unDocumentoDistintoSeRechaza() {
        Cliente cliente = conDocumento();
        assertThrows(MiException.class, () -> ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, TipoDocumento.DNI, "35111223"));
        assertThrows(MiException.class, () -> ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, TipoDocumento.CUIL, "35111222"));
        assertEquals("35111222", cliente.getNumeroDocumento());
        assertEquals(TipoDocumento.DNI, cliente.getTipoDocumento());
    }

    @Test
    void elMismoDocumentoConPuntosOVacioLoConserva() throws MiException {
        Cliente cliente = conDocumento();
        ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, TipoDocumento.DNI, "35.111.222");
        ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, null, "  ");
        ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, null, null);
        assertEquals("35111222", cliente.getNumeroDocumento());
        assertEquals(TipoDocumento.DNI, cliente.getTipoDocumento());
    }

    @Test
    void quienNoTeniaDocumentoLoPuedeCompletar() throws MiException {
        Cliente cliente = new Cliente(); // por ejemplo, un cliente que ingresó con Google
        ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, TipoDocumento.DNI, "40.123.456");
        assertEquals("40123456", cliente.getNumeroDocumento());
        assertEquals(TipoDocumento.DNI, cliente.getTipoDocumento());
    }
}

package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.RegistroClienteDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioClienteTest {

    private RepositorioCliente repositorio;
    private ServicioUsuario servicioUsuario;
    private ServicioCliente servicio;

    @BeforeEach
    void setUp() throws MiException {
        repositorio = mock(RepositorioCliente.class);
        servicioUsuario = mock(ServicioUsuario.class);
        servicio = new ServicioCliente(repositorio, servicioUsuario, mock(ServicioDireccion.class));
        when(repositorio.save(any(Cliente.class))).then(returnsFirstArg());
        when(repositorio.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(any(), any())).thenReturn(Optional.empty());
        when(servicioUsuario.crearUsuario(anyString(), any(), any(), any())).thenAnswer(invocacion ->
                DatosPersonas.usuario("u-1", invocacion.getArgument(0), invocacion.getArgument(1),
                        invocacion.getArgument(3), null));
    }

    @Test
    void elRegistroCreaUnaCuentaLocalConRolCliente() throws MiException {
        Cliente cliente = servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez", "marta@kiosco.com",
                "Golosinas2026", "Golosinas2026", null, null, null));

        assertEquals("Marta", cliente.getNombre());
        verify(servicioUsuario).crearUsuario("marta@kiosco.com", RolUsuario.CLIENTE, "Golosinas2026",
                ProveedorAutenticacion.LOCAL);
    }

    @Test
    void laConfirmacionDeLaClaveDebeCoincidir() throws MiException {
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Otra2026", null, null, null)));
        verify(servicioUsuario, never()).crearUsuario(anyString(), any(), any(), any());
    }

    @Test
    void elDocumentoEsOpcionalPeroDebeVenirCompleto() {
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.DNI, null, null)));
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.CUIT, "20-123", null)));
    }

    @Test
    void elRegistroSocialLimpiaLosNombresQueEnviaElProveedor() throws MiException {
        Cliente cliente = servicio.registrarClienteRedSocial("ana.perez@gmail.com", null, "P3rez!!",
                ProveedorAutenticacion.GOOGLE);

        assertEquals("anaperez", cliente.getNombre());
        assertEquals("Prez", cliente.getApellido());
        verify(servicioUsuario).crearUsuario("ana.perez@gmail.com", RolUsuario.CLIENTE, null, ProveedorAutenticacion.GOOGLE);
    }
}

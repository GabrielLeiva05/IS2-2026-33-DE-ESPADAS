package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ClienteCampoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RegistroClienteDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioClienteTest {

    private static final LocalDate ADULTA = LocalDate.of(1990, 4, 12);

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
                "Golosinas2026", "Golosinas2026", TipoDocumento.DNI, "31.222.333", ADULTA));

        assertEquals("Marta", cliente.getNombre());
        assertEquals("31222333", cliente.getNumeroDocumento());
        verify(servicioUsuario).crearUsuario("marta@kiosco.com", RolUsuario.CLIENTE, "Golosinas2026",
                ProveedorAutenticacion.LOCAL);
    }

    @Test
    void laConfirmacionDeLaClaveDebeCoincidir() throws MiException {
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Otra2026", TipoDocumento.DNI, "31222333", ADULTA)));
        verify(servicioUsuario, never()).crearUsuario(anyString(), any(), any(), any());
    }

    @Test
    void elRegistroInstitucionalExigeDocumentoYFechaDeNacimiento() {
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", null, null, ADULTA)));
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.DNI, null, ADULTA)));
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.CUIT, "20-123", ADULTA)));
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.DNI, "31222333", null)));
        assertThrows(MiException.class, () -> servicio.registrarCliente(new RegistroClienteDTO("Marta", "Suárez",
                "marta@kiosco.com", "Golosinas2026", "Golosinas2026", TipoDocumento.DNI, "31222333",
                LocalDate.now().minusYears(17))));
    }

    @Test
    void elAltaEnCampoUsaLaClaveInicialYExigeDocumentoYFecha() throws MiException {
        when(servicioUsuario.crearCuentaClienteConClaveTemporal(anyString())).thenAnswer(invocacion ->
                DatosPersonas.usuario("u-2", invocacion.getArgument(0), RolUsuario.CLIENTE, ProveedorAutenticacion.LOCAL, null));

        assertThrows(MiException.class, () -> servicio.registrarClienteEnCampo(new ClienteCampoDTO("Ana", "López",
                "ana@kiosco.com", null, null, ADULTA, null)));
        assertThrows(MiException.class, () -> servicio.registrarClienteEnCampo(new ClienteCampoDTO("Ana", "López",
                "ana@kiosco.com", TipoDocumento.DNI, "35111222", null, null)));
        verify(servicioUsuario, never()).crearCuentaClienteConClaveTemporal(anyString());

        assertEquals("Ana", servicio.registrarClienteEnCampo(new ClienteCampoDTO("Ana", "López",
                "ana@kiosco.com", TipoDocumento.DNI, "35111222", ADULTA, null)).nombre());
        verify(servicioUsuario).crearCuentaClienteConClaveTemporal("ana@kiosco.com");
        verify(servicioUsuario, never()).crearUsuario(anyString(), any(), any(), any());
    }

    @Test
    void elDocumentoRegistradoNoSeModifica() throws MiException {
        Cliente cliente = new Cliente();
        cliente.setId("c-1");
        cliente.setNombre("Ana");
        cliente.setApellido("López");
        cliente.setTipoDocumento(TipoDocumento.DNI);
        cliente.setNumeroDocumento("35111222");
        cliente.setUsuario(DatosPersonas.usuario("u-3", "ana@kiosco.com", RolUsuario.CLIENTE, ProveedorAutenticacion.LOCAL, "HASH"));
        when(repositorio.findByIdAndEliminadoFalse("c-1")).thenReturn(Optional.of(cliente));

        MiException error = assertThrows(MiException.class, () -> servicio.modificar("c-1",
                new com.ejercicioIntegrador.dulcemayor.dto.ClienteRequestDTO("Ana", "López", ADULTA, TipoDocumento.DNI,
                        "99999999", "ana@kiosco.com", null, null, false)));
        assertTrue(error.getMessage().contains("no se puede modificar"));
        assertEquals("35111222", cliente.getNumeroDocumento());
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

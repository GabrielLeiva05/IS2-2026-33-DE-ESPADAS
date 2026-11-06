package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCliente;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPuntoVenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioPuntoVentaTest {

    private static final String CORREO = "preventista@dulcemayor.test";

    private RepositorioPuntoVenta repositorioPuntoVenta;
    private RepositorioCliente repositorioCliente;
    private ServicioEmpleado servicioEmpleado;
    private ServicioUsuario servicioUsuario;
    private ServicioDireccion servicioDireccion;
    private ServicioPuntoVenta servicio;

    private Preventista preventista;
    private Direccion direccion;

    @BeforeEach
    void setUp() throws MiException {
        repositorioPuntoVenta = mock(RepositorioPuntoVenta.class);
        repositorioCliente = mock(RepositorioCliente.class);
        servicioEmpleado = mock(ServicioEmpleado.class);
        servicioUsuario = mock(ServicioUsuario.class);
        servicioDireccion = mock(ServicioDireccion.class);
        servicio = new ServicioPuntoVenta(repositorioPuntoVenta, repositorioCliente, servicioEmpleado,
                servicioUsuario, servicioDireccion);

        preventista = new Preventista();
        preventista.setId("prev-1");
        direccion = new Direccion();
        direccion.setId("dir-1");

        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO))
                .thenReturn(Optional.of(cuenta("usr-1", RolUsuario.PREVENTISTA)));
        when(servicioEmpleado.buscarPorUsuario("usr-1")).thenReturn(Optional.of(preventista));
        when(repositorioCliente.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                eq(TipoDocumento.DNI), any(String.class))).thenReturn(Optional.empty());
        when(repositorioCliente.save(any(Cliente.class))).then(returnsFirstArg());
        when(servicioDireccion.crearDireccion(any(DireccionRequestDTO.class))).thenReturn(direccion);
        when(repositorioPuntoVenta.save(any(PuntoVenta.class))).then(returnsFirstArg());
    }

    @Test
    void registrarAsociaElPuntoAlPreventistaAutenticadoYCreaElResponsable() throws MiException {
        PuntoVentaResponseDTO creado = servicio.registrar(solicitud("40123456"), CORREO);

        assertEquals("Kiosco Norte", creado.nombreComercial());
        assertEquals("prev-1", creado.idPreventista());
        assertEquals("Carlos", creado.nombreCliente());
        assertEquals("2615551234", creado.telefono());
        verify(repositorioCliente).save(any(Cliente.class));
        verify(repositorioPuntoVenta).save(any(PuntoVenta.class));
    }

    @Test
    void registrarRechazaUnDocumentoQueYaExisteSinCrearNada() throws MiException {
        Cliente existente = new Cliente();
        existente.setId("cli-1");
        when(repositorioCliente.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                eq(TipoDocumento.DNI), any(String.class))).thenReturn(Optional.of(existente));

        MiException error = assertThrows(MiException.class, () -> servicio.registrar(solicitud("40123456"), CORREO));

        assertTrue(error.getMessage().contains("Ya existe un responsable"));
        verify(repositorioCliente, never()).save(any(Cliente.class));
        verify(repositorioPuntoVenta, never()).save(any(PuntoVenta.class));
    }

    @Test
    void registrarRechazaUnaCuentaQueNoTienePerfilDePreventista() throws MiException {
        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO))
                .thenReturn(Optional.of(cuenta("usr-9", RolUsuario.DISTRIBUIDOR)));

        assertThrows(MiException.class, () -> servicio.registrar(solicitud("40123456"), CORREO));
        verify(repositorioPuntoVenta, never()).save(any(PuntoVenta.class));
    }

    @Test
    void registrarRechazaAlPreventistaSinEmpleadoAsociado() throws MiException {
        when(servicioEmpleado.buscarPorUsuario("usr-1")).thenReturn(Optional.empty());

        assertThrows(MiException.class, () -> servicio.registrar(solicitud("40123456"), CORREO));
        verify(repositorioPuntoVenta, never()).save(any(PuntoVenta.class));
    }

    @Test
    void registrarExigeLosDatosMinimosDelResponsableYDelComercio() {
        PuntoVentaRequestDTO sinComercio = new PuntoVentaRequestDTO("Carlos", "Gómez",
                "carlos@example.test", LocalDate.of(1985, 3, 12), TipoDocumento.DNI, "40123456", "",
                null, null, direccionRequest());

        assertThrows(MiException.class, () -> servicio.registrar(sinComercio, CORREO));
        assertThrows(MiException.class, () -> servicio.registrar(null, CORREO));
    }

    @Test
    void unPuntoDeOtroPreventistaNoSePuedeUsarEnUnPedido() throws MiException {
        Preventista otro = new Preventista();
        otro.setId("prev-2");
        PuntoVenta ajeno = new PuntoVenta();
        ajeno.setId("pv-2");
        ajeno.setPreventista(otro);
        when(repositorioPuntoVenta.findByIdAndEliminadoFalse("pv-2")).thenReturn(Optional.of(ajeno));

        assertThrows(MiException.class, () -> servicio.obtenerPuntoPropio("pv-2", CORREO));
    }

    @Test
    void elJefeConsultaLaCarteraDeTodosLosPreventistas() throws MiException {
        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO))
                .thenReturn(Optional.of(cuenta("usr-jefe", RolUsuario.JEFE)));
        when(repositorioPuntoVenta.findByEliminadoFalse())
                .thenReturn(List.of(puntoVenta("pv-b", "Zona Sur"), puntoVenta("pv-a", "Kiosco Norte")));

        List<PuntoVentaResponseDTO> cartera = servicio.listarCartera(CORREO);

        assertEquals(2, cartera.size());
        assertEquals("Kiosco Norte", cartera.get(0).nombreComercial());
    }

    @Test
    void elJefeNoRegistraPuntosPorqueNoTieneCarteraPropia() throws MiException {
        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO))
                .thenReturn(Optional.of(cuenta("usr-jefe", RolUsuario.JEFE)));

        assertThrows(MiException.class, () -> servicio.registrar(solicitud("40123456"), CORREO));
        verify(repositorioPuntoVenta, never()).save(any(PuntoVenta.class));
    }

    private Usuario cuenta(String id, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        return usuario;
    }

    private PuntoVenta puntoVenta(String id, String nombreComercial) {
        PuntoVenta punto = new PuntoVenta();
        punto.setId(id);
        punto.setNombreComercial(nombreComercial);
        punto.setPreventista(preventista);
        Cliente cliente = new Cliente();
        cliente.setId("cli-" + id);
        cliente.setNombre("Carlos");
        cliente.setApellido("Gómez");
        punto.setCliente(cliente);
        punto.setDireccion(direccion);
        return punto;
    }

    private DireccionRequestDTO direccionRequest() {
        return new DireccionRequestDTO("San Martín", "123", null, null, null, null, -32.89, -68.84, "loc-1");
    }

    private PuntoVentaRequestDTO solicitud(String documento) {
        return new PuntoVentaRequestDTO("Carlos", "Gómez", "carlos@example.test", LocalDate.of(1985, 3, 12),
                TipoDocumento.DNI, documento, "Kiosco Norte", "2615551234", "kiosco@example.test",
                direccionRequest());
    }
}

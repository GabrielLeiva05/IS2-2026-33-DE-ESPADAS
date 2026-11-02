package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioEmpleado;
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

class ServicioEmpleadoTest {

    private RepositorioEmpleado repositorio;
    private ServicioUsuario servicioUsuario;
    private ServicioDireccion servicioDireccion;
    private ServicioEmpleado servicio;

    @BeforeEach
    void setUp() throws MiException {
        repositorio = mock(RepositorioEmpleado.class);
        servicioUsuario = mock(ServicioUsuario.class);
        servicioDireccion = mock(ServicioDireccion.class);
        servicio = new ServicioEmpleado(repositorio, servicioUsuario, servicioDireccion, mock(ServicioImagen.class));

        when(repositorio.save(any(Empleado.class))).then(returnsFirstArg());
        when(repositorio.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(any(), any())).thenReturn(Optional.empty());
        when(repositorio.findFirstByLegajoIgnoreCaseAndEliminadoFalse(any())).thenReturn(Optional.empty());
        when(servicioDireccion.crearDireccion(any())).thenReturn(DatosPersonas.direccionGuardada());
        for (RolUsuario rol : RolUsuario.values()) {
            when(servicioUsuario.crearUsuario(anyString(), org.mockito.ArgumentMatchers.eq(rol), any(), any()))
                    .thenReturn(DatosPersonas.usuario("u-" + rol, "empleado@dulcemayor.com", rol,
                            ProveedorAutenticacion.LOCAL, "HASH(dulcemayor)"));
        }
    }

    @Test
    void elAltaDeUnPreventistaCreaLaSubclaseLaDireccionYLaCuenta() throws MiException {
        EmpleadoResponseDTO creado = servicio.alta(DatosPersonas.empleado(RolUsuario.PREVENTISTA, "ana@dulcemayor.com"));

        assertEquals(RolUsuario.PREVENTISTA, creado.rol());
        assertEquals("Zona Este", creado.zona());
        assertEquals("30111222", creado.numeroDocumento());
        assertEquals("EMP-00001", creado.legajo());
        assertEquals(LocalDate.now(), creado.fechaIngreso());
        verify(servicioUsuario).crearUsuario("ana@dulcemayor.com", RolUsuario.PREVENTISTA, null, ProveedorAutenticacion.LOCAL);
    }

    @Test
    void elDistribuidorNormalizaYValidaLaPatente() throws MiException {
        EmpleadoResponseDTO creado = servicio.alta(DatosPersonas.empleado(RolUsuario.DISTRIBUIDOR, "luis@dulcemayor.com"));
        assertEquals("AB123CD", creado.patente());

        EmpleadoRequestDTO patenteInvalida = new EmpleadoRequestDTO("Luis", "Gómez", LocalDate.of(1985, 1, 1),
                TipoDocumento.DNI, "28111222", "luis2@dulcemayor.com", RolUsuario.DISTRIBUIDOR, null, null,
                DatosPersonas.direccion(), null, "Camioneta", "1234", null);
        assertThrows(MiException.class, () -> servicio.alta(patenteInvalida));
    }

    @Test
    void noSePuedeDarDeAltaOtroJefe() {
        when(repositorio.existsByUsuario_RolAndEliminadoFalse(RolUsuario.JEFE)).thenReturn(true);

        MiException error = assertThrows(MiException.class,
                () -> servicio.alta(DatosPersonas.empleado(RolUsuario.JEFE, "otro@dulcemayor.com")));

        assertTrue(error.getMessage().startsWith("Ya existe un jefe administrativo"));
        verify(repositorio, never()).save(any(Empleado.class));
    }

    @Test
    void elEmpleadoDebeSerMayorDeEdad() {
        EmpleadoRequestDTO menor = new EmpleadoRequestDTO("Ana", "Pérez", LocalDate.now().minusYears(16),
                TipoDocumento.DNI, "45111222", "ana@dulcemayor.com", RolUsuario.ADMINISTRATIVO, null, null,
                DatosPersonas.direccion(), null, null, null, null);

        assertThrows(MiException.class, () -> servicio.alta(menor));
    }

    @Test
    void elDocumentoNoSePuedeRepetir() {
        Preventista existente = new Preventista();
        existente.setId("emp-9");
        when(repositorio.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(TipoDocumento.DNI, "30111222"))
                .thenReturn(Optional.of(existente));

        assertThrows(MiException.class,
                () -> servicio.alta(DatosPersonas.empleado(RolUsuario.PREVENTISTA, "ana@dulcemayor.com")));
    }

    @Test
    void laBajaDesactivaLaCuentaDelEmpleado() throws MiException {
        Distribuidor distribuidor = new Distribuidor();
        distribuidor.setId("emp-1");
        Usuario cuenta = DatosPersonas.usuario("u-1", "luis@dulcemayor.com", RolUsuario.DISTRIBUIDOR,
                ProveedorAutenticacion.LOCAL, "HASH(x)");
        distribuidor.setUsuario(cuenta);
        when(repositorio.findByIdAndEliminadoFalse("emp-1")).thenReturn(Optional.of(distribuidor));

        servicio.baja("emp-1");

        assertTrue(distribuidor.isEliminado());
        verify(servicioUsuario).baja("u-1");
    }

    @Test
    void elJefeNoSePuedeDarDeBaja() {
        Empleado jefe = new Empleado();
        jefe.setId("emp-1");
        jefe.setUsuario(DatosPersonas.usuario("u-1", "jefe@dulcemayor.com", RolUsuario.JEFE,
                ProveedorAutenticacion.LOCAL, "HASH(x)"));
        when(repositorio.findByIdAndEliminadoFalse("emp-1")).thenReturn(Optional.of(jefe));

        assertThrows(OperacionNoPermitidaException.class, () -> servicio.baja("emp-1"));
    }

    @Test
    void enLaModificacionNoSePuedeCambiarElRolNiElCorreo() {
        Preventista preventista = new Preventista();
        preventista.setId("emp-1");
        preventista.setUsuario(DatosPersonas.usuario("u-1", "ana@dulcemayor.com", RolUsuario.PREVENTISTA,
                ProveedorAutenticacion.LOCAL, "HASH(x)"));
        when(repositorio.findByIdAndEliminadoFalse("emp-1")).thenReturn(Optional.of(preventista));

        assertThrows(MiException.class, () -> servicio.modificar("emp-1",
                DatosPersonas.empleado(RolUsuario.DISTRIBUIDOR, "ana@dulcemayor.com")));
        assertThrows(MiException.class, () -> servicio.modificar("emp-1",
                DatosPersonas.empleado(RolUsuario.PREVENTISTA, "otro@dulcemayor.com")));
    }
}

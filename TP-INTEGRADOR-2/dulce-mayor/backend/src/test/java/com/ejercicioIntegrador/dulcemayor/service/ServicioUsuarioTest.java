package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioUsuarioTest {

    private RepositorioUsuario repositorio;
    private ServicioUsuario servicio;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioUsuario.class);
        servicio = new ServicioUsuario(repositorio, DatosPersonas.CODIFICADOR);
        when(repositorio.save(any(Usuario.class))).then(returnsFirstArg());
        when(repositorio.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(any())).thenReturn(Optional.empty());
    }

    @Test
    void elEmpleadoRecibeLaClaveTemporalHasheadaYDebeCambiarla() throws MiException {
        Usuario usuario = servicio.crearUsuario("  Ana@Mail.COM ", RolUsuario.PREVENTISTA, "ignorada",
                ProveedorAutenticacion.LOCAL);

        assertEquals("ana@mail.com", usuario.getNombreUsuario());
        assertEquals("HASH(dulcemayor)", usuario.getClave());
        assertTrue(usuario.isDebeCambiarClave());
    }

    @Test
    void elClienteEligeSuClaveYNoDebeCambiarla() throws MiException {
        Usuario usuario = servicio.crearUsuario("cliente@mail.com", RolUsuario.CLIENTE, "Golosinas2026",
                ProveedorAutenticacion.LOCAL);

        assertEquals("HASH(Golosinas2026)", usuario.getClave());
        assertFalse(usuario.isDebeCambiarClave());
    }

    @Test
    void laClaveDelClienteDebeCumplirLaPolitica() {
        assertThrows(MiException.class, () -> servicio.crearUsuario("c@mail.com", RolUsuario.CLIENTE, "corta1",
                ProveedorAutenticacion.LOCAL));
        assertThrows(MiException.class, () -> servicio.crearUsuario("c@mail.com", RolUsuario.CLIENTE, "sololetras",
                ProveedorAutenticacion.LOCAL));
        assertThrows(MiException.class, () -> servicio.crearUsuario("c@mail.com", RolUsuario.CLIENTE, "dulcemayor",
                ProveedorAutenticacion.LOCAL));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void laCuentaDeRedSocialNoTieneClave() throws MiException {
        Usuario usuario = servicio.crearUsuario("cliente@gmail.com", RolUsuario.CLIENTE, null,
                ProveedorAutenticacion.GOOGLE);

        assertNull(usuario.getClave());
        assertFalse(usuario.isDebeCambiarClave());
    }

    @Test
    void unEmpleadoNoPuedeTenerUnaCuentaDeRedSocial() {
        assertThrows(MiException.class, () -> servicio.crearUsuario("empleado@gmail.com", RolUsuario.ADMINISTRATIVO,
                null, ProveedorAutenticacion.GOOGLE));
    }

    @Test
    void noSePuedeRepetirElCorreo() {
        when(repositorio.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse("ana@mail.com"))
                .thenReturn(Optional.of(DatosPersonas.usuario("u-9", "ana@mail.com", RolUsuario.CLIENTE,
                        ProveedorAutenticacion.LOCAL, "HASH(x)")));

        assertThrows(MiException.class, () -> servicio.crearUsuario("ANA@mail.com", RolUsuario.PREVENTISTA, null,
                ProveedorAutenticacion.LOCAL));
    }

    @Test
    void cambiarLaClaveApagaElIndicadorYLaGuardaHasheada() throws MiException {
        Usuario usuario = DatosPersonas.usuario("u-1", "ana@mail.com", RolUsuario.PREVENTISTA,
                ProveedorAutenticacion.LOCAL, "HASH(dulcemayor)");
        usuario.setDebeCambiarClave(true);
        when(repositorio.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse("ana@mail.com")).thenReturn(Optional.of(usuario));

        servicio.modificarClave("ana@mail.com", "dulcemayor", "NuevaClave1", "NuevaClave1");

        assertEquals("HASH(NuevaClave1)", usuario.getClave());
        assertFalse(usuario.isDebeCambiarClave());
    }

    @Test
    void cambiarLaClaveValidaLaActualLaConfirmacionYQueSeaDistinta() {
        Usuario usuario = DatosPersonas.usuario("u-1", "ana@mail.com", RolUsuario.PREVENTISTA,
                ProveedorAutenticacion.LOCAL, "HASH(Actual2026)");
        when(repositorio.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse("ana@mail.com")).thenReturn(Optional.of(usuario));

        assertThrows(MiException.class, () -> servicio.modificarClave("ana@mail.com", "otra", "NuevaClave1", "NuevaClave1"));
        assertThrows(MiException.class, () -> servicio.modificarClave("ana@mail.com", "Actual2026", "NuevaClave1", "Distinta1"));
        assertThrows(MiException.class, () -> servicio.modificarClave("ana@mail.com", "Actual2026", "Actual2026", "Actual2026"));
        assertThrows(MiException.class, () -> servicio.modificarClave("ana@mail.com", "Actual2026", "dulcemayor", "dulcemayor"));
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    void lasCuentasNoSeModificanPorLaOperacionGenerica() {
        when(repositorio.findByIdAndEliminadoFalse("u-1")).thenReturn(Optional.of(DatosPersonas.usuario("u-1",
                "ana@mail.com", RolUsuario.PREVENTISTA, ProveedorAutenticacion.LOCAL, "HASH(x)")));

        assertThrows(MiException.class, () -> servicio.modificar("u-1", null));
    }
}

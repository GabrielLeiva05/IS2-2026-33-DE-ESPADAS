package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.CanjeCodigoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.IdentidadExternaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LoginRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SesionDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.security.CodigosCanjeOAuth2;
import com.ejercicioIntegrador.dulcemayor.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioAutenticacionTest {

    private AuthenticationManager authenticationManager;
    private ServicioUsuario servicioUsuario;
    private ServicioCliente servicioCliente;
    private ServicioAutenticacion servicio;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        servicioUsuario = mock(ServicioUsuario.class);
        servicioCliente = mock(ServicioCliente.class);
        servicio = new ServicioAutenticacion(authenticationManager, jwtService, new CodigosCanjeOAuth2(),
                servicioUsuario, mock(ServicioEmpleado.class), servicioCliente);
        when(jwtService.generarToken(any(), any(), any())).thenReturn("jwt");
        when(jwtService.getExpiracionSegundos()).thenReturn(3600L);
    }

    private Usuario cuenta(String correo, RolUsuario rol, ProveedorAutenticacion proveedor) {
        Usuario usuario = DatosPersonas.usuario("u-1", correo, rol, proveedor, null);
        when(servicioUsuario.buscarCualquieraPorNombreUsuario(correo)).thenReturn(Optional.of(usuario));
        when(servicioUsuario.buscarActivoPorNombreUsuario(correo)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    void elLoginNormalizaElCorreoYDevuelveElToken() throws MiException {
        Usuario usuario = cuenta("ana@dulcemayor.com", RolUsuario.PREVENTISTA, ProveedorAutenticacion.LOCAL);
        usuario.setDebeCambiarClave(true);

        SesionDTO sesion = servicio.login(new LoginRequestDTO("  ANA@dulcemayor.com ", "dulcemayor"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("ana@dulcemayor.com", "dulcemayor"));
        assertEquals("jwt", sesion.token());
        assertTrue(sesion.usuario().debeCambiarClave());
    }

    @Test
    void unCorreoNuevoDeGoogleRegistraUnCliente() throws MiException {
        when(servicioUsuario.buscarCualquieraPorNombreUsuario("nuevo@gmail.com")).thenReturn(Optional.empty());
        Cliente cliente = new Cliente();
        cliente.setUsuario(DatosPersonas.usuario("u-2", "nuevo@gmail.com", RolUsuario.CLIENTE,
                ProveedorAutenticacion.GOOGLE, null));
        when(servicioCliente.registrarClienteRedSocial("nuevo@gmail.com", "Ana", "Pérez", ProveedorAutenticacion.GOOGLE))
                .thenReturn(cliente);

        Usuario usuario = servicio.ingresarConRedSocial(new IdentidadExternaDTO("Nuevo@Gmail.com", true, "Ana", "Pérez",
                ProveedorAutenticacion.GOOGLE));

        assertSame(cliente.getUsuario(), usuario);
    }

    @Test
    void googleDebeInformarElCorreoVerificado() throws MiException {
        assertThrows(MiException.class, () -> servicio.ingresarConRedSocial(new IdentidadExternaDTO("x@gmail.com",
                false, "Ana", "Pérez", ProveedorAutenticacion.GOOGLE)));
        verify(servicioCliente, never()).registrarClienteRedSocial(any(), any(), any(), any());
    }

    @Test
    void unEmpleadoNoPuedeIngresarConUnaRedSocial() {
        cuenta("jefe@gmail.com", RolUsuario.JEFE, ProveedorAutenticacion.LOCAL);

        MiException error = assertThrows(MiException.class, () -> servicio.ingresarConRedSocial(
                new IdentidadExternaDTO("jefe@gmail.com", true, "J", "J", ProveedorAutenticacion.GOOGLE)));

        assertTrue(error.getMessage().contains("empleados"));
    }

    @Test
    void siElCorreoYaTieneUnaCuentaLocalSeRechazaYSeAvisa() {
        cuenta("marta@gmail.com", RolUsuario.CLIENTE, ProveedorAutenticacion.LOCAL);

        MiException error = assertThrows(MiException.class, () -> servicio.ingresarConRedSocial(
                new IdentidadExternaDTO("marta@gmail.com", true, "Marta", "S", ProveedorAutenticacion.GOOGLE)));

        assertEquals("Ya existe una cuenta con ese correo: ingresá con tu contraseña", error.getMessage());
    }

    @Test
    void siElCorreoEsDeOtroProveedorSeRechaza() {
        cuenta("marta@gmail.com", RolUsuario.CLIENTE, ProveedorAutenticacion.FACEBOOK);

        assertThrows(MiException.class, () -> servicio.ingresarConRedSocial(
                new IdentidadExternaDTO("marta@gmail.com", true, "Marta", "S", ProveedorAutenticacion.GOOGLE)));
    }

    @Test
    void conElMismoProveedorIngresaALaCuentaExistente() throws MiException {
        Usuario existente = cuenta("marta@gmail.com", RolUsuario.CLIENTE, ProveedorAutenticacion.GOOGLE);

        assertSame(existente, servicio.ingresarConRedSocial(
                new IdentidadExternaDTO("marta@gmail.com", true, "Marta", "S", ProveedorAutenticacion.GOOGLE)));
    }

    @Test
    void unaCuentaDadaDeBajaNoPuedeIngresar() {
        Usuario existente = cuenta("marta@gmail.com", RolUsuario.CLIENTE, ProveedorAutenticacion.GOOGLE);
        existente.setEliminado(true);

        assertThrows(MiException.class, () -> servicio.ingresarConRedSocial(
                new IdentidadExternaDTO("marta@gmail.com", true, "Marta", "S", ProveedorAutenticacion.GOOGLE)));
    }

    @Test
    void elCodigoDeIngresoSeCanjeaUnaSolaVez() throws MiException {
        Usuario usuario = cuenta("marta@gmail.com", RolUsuario.CLIENTE, ProveedorAutenticacion.GOOGLE);
        String codigo = servicio.emitirCodigoCanje(usuario);

        assertEquals("jwt", servicio.canjearCodigo(new CanjeCodigoDTO(codigo)).token());
        assertThrows(MiException.class, () -> servicio.canjearCodigo(new CanjeCodigoDTO(codigo)));
        assertThrows(MiException.class, () -> servicio.canjearCodigo(new CanjeCodigoDTO("inventado")));
    }
}

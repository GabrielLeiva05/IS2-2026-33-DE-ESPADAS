package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.CambioClaveRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CanjeCodigoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.IdentidadExternaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LoginRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RegistroClienteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SesionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UsuarioSesionDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Persona;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.security.CodigosCanjeOAuth2;
import com.ejercicioIntegrador.dulcemayor.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Casos de uso de autenticación: login, registro de clientes, cambio de clave e ingreso con redes sociales.
 * No hereda de BaseService porque no administra una entidad propia (mismo criterio que ServicioCatalogo):
 * coordina ServicioUsuario, ServicioEmpleado y ServicioCliente.
 * login(...) y loginRedSocial(...) del diagrama de Usuario se implementan acá, con Spring Security.
 */
@Service
public class ServicioAutenticacion {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CodigosCanjeOAuth2 codigosCanje;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioCliente servicioCliente;

    public ServicioAutenticacion(AuthenticationManager authenticationManager, JwtService jwtService,
                                 CodigosCanjeOAuth2 codigosCanje, ServicioUsuario servicioUsuario,
                                 ServicioEmpleado servicioEmpleado, ServicioCliente servicioCliente) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.codigosCanje = codigosCanje;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioCliente = servicioCliente;
    }

    /**
     * Ingreso con correo y contraseña. Si las credenciales son incorrectas, Spring Security lanza
     * BadCredentialsException (401). Si el empleado tiene la clave temporal, el token se emite igual
     * pero solo le permite cambiar la clave (la respuesta indica debeCambiarClave = true).
     */
    @Transactional(readOnly = true)
    public SesionDTO login(LoginRequestDTO login) throws MiException {
        if (login == null || login.correo() == null || login.correo().isBlank()
                || login.clave() == null || login.clave().isEmpty()) {
            throw new MiException("Debe indicar el correo y la contraseña");
        }
        String correo = ServicioUsuario.normalizarCorreo(login.correo());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, login.clave()));
        return crearSesion(cuentaActiva(correo));
    }

    /** Registro público de clientes: crea el cliente con rol CLIENTE y lo deja autenticado. */
    @Transactional(rollbackFor = Exception.class)
    public SesionDTO registrarCliente(RegistroClienteDTO registro) throws MiException {
        return crearSesion(servicioCliente.registrarCliente(registro).getUsuario());
    }

    /** Cambio de clave (obligatorio o voluntario). Devuelve un token nuevo con los permisos del rol. */
    @Transactional(rollbackFor = Exception.class)
    public SesionDTO cambiarClave(String correo, CambioClaveRequestDTO cambio) throws MiException {
        if (cambio == null) {
            throw new MiException("Debe indicar la contraseña actual y la nueva");
        }
        Usuario usuario = servicioUsuario.modificarClave(correo, cambio.claveActual(), cambio.nuevaClave(),
                cambio.confirmarClave());
        return crearSesion(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioSesionDTO usuarioActual(String correo) throws MiException {
        return datosDeSesion(cuentaActiva(correo));
    }

    /**
     * loginRedSocial(token, proveedor) del diagrama. La identidad ya fue validada por Spring Security (OAuth2/OIDC).
     * Reglas (decisión del equipo: rechazar y avisar ante un correo ya registrado):
     *  - Google debe informar el correo como verificado.
     *  - Si no existe una cuenta con ese correo, se registra un cliente nuevo.
     *  - Si existe con el mismo proveedor y rol CLIENTE, ingresa.
     *  - En cualquier otro caso (empleado, cuenta local, otro proveedor o cuenta dada de baja) se rechaza.
     */
    @Transactional(rollbackFor = Exception.class)
    public Usuario ingresarConRedSocial(IdentidadExternaDTO identidad) throws MiException {
        if (identidad == null || identidad.proveedor() == null || identidad.proveedor() == ProveedorAutenticacion.LOCAL) {
            throw new MiException("Proveedor de autenticación no soportado");
        }
        if (identidad.correo() == null || identidad.correo().isBlank()) {
            throw new MiException("El proveedor no informó un correo electrónico. Revisá los permisos otorgados.");
        }
        if (identidad.proveedor() == ProveedorAutenticacion.GOOGLE && !identidad.correoVerificado()) {
            throw new MiException("Google no confirmó que tu correo esté verificado");
        }
        String correo = ServicioUsuario.normalizarCorreo(identidad.correo());
        Optional<Usuario> existente = servicioUsuario.buscarCualquieraPorNombreUsuario(correo);
        if (existente.isEmpty()) {
            return servicioCliente.registrarClienteRedSocial(correo, identidad.nombre(), identidad.apellido(),
                    identidad.proveedor()).getUsuario();
        }
        Usuario usuario = existente.get();
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new MiException("Los empleados ingresan con correo y contraseña desde el sistema administrativo");
        }
        if (usuario.isEliminado()) {
            throw new MiException("La cuenta asociada a ese correo está dada de baja");
        }
        if (usuario.getProveedorAutenticacion() != identidad.proveedor()) {
            throw new MiException(usuario.getProveedorAutenticacion() == ProveedorAutenticacion.LOCAL
                    ? "Ya existe una cuenta con ese correo: ingresá con tu contraseña"
                    : "Ya existe una cuenta con ese correo: ingresá con " + nombreProveedor(usuario.getProveedorAutenticacion()));
        }
        return usuario;
    }

    /** Emite el código de un solo uso con el que el sitio institucional obtiene el token. */
    public String emitirCodigoCanje(Usuario usuario) {
        return codigosCanje.emitir(usuario.getNombreUsuario());
    }

    @Transactional(readOnly = true)
    public SesionDTO canjearCodigo(CanjeCodigoDTO canje) throws MiException {
        String correo = codigosCanje.canjear(canje == null ? null : canje.codigo())
                .orElseThrow(() -> new MiException("El código de ingreso no es válido o venció. Volvé a intentarlo."));
        return crearSesion(cuentaActiva(correo));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Usuario cuentaActiva(String correo) throws MiException {
        return servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta activa con ese correo"));
    }

    private SesionDTO crearSesion(Usuario usuario) {
        String token = jwtService.generarToken(usuario.getNombreUsuario(), usuario.getRol(), usuario.getId());
        return SesionDTO.bearer(token, jwtService.getExpiracionSegundos(), datosDeSesion(usuario));
    }

    private UsuarioSesionDTO datosDeSesion(Usuario usuario) {
        Optional<? extends Persona> persona = usuario.getRol() == RolUsuario.CLIENTE
                ? servicioCliente.buscarClientePorUsuario(usuario.getId())
                : servicioEmpleado.buscarPorUsuario(usuario.getId());
        return new UsuarioSesionDTO(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(),
                usuario.isDebeCambiarClave(), usuario.getProveedorAutenticacion(),
                persona.map(Persona::getNombre).orElse(null), persona.map(Persona::getApellido).orElse(null));
    }

    private static String nombreProveedor(ProveedorAutenticacion proveedor) {
        return proveedor == ProveedorAutenticacion.GOOGLE ? "Google" : "Facebook";
    }
}

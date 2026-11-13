package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.UsuarioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UsuarioResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Cuentas de acceso. Reglas principales:
 *  - Empleados: se crean con la clave temporal "dulcemayor" y debeCambiarClave = true (nota del diagrama).
 *  - Clientes con registro local en el sitio: eligen su clave y debeCambiarClave = false.
 *  - Clientes dados de alta por el preventista (app móvil): clave temporal "dulcemayor" y debeCambiarClave = true.
 *  - Clientes de Google o Facebook: sin clave.
 *  - La clave se guarda siempre con hashing (BCrypt), nunca en texto plano.
 * Las cuentas no se crean por un endpoint propio: las crean ServicioEmpleado y ServicioCliente.
 */
@Service
public class ServicioUsuario extends BaseService<Usuario, UsuarioRequestDTO, UsuarioResponseDTO> {

    /** Clave temporal que el enunciado asigna a todo empleado nuevo. */
    public static final String CLAVE_TEMPORAL = "dulcemayor";

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int LARGO_MINIMO_CLAVE = 8;
    /** BCrypt solo usa los primeros 72 bytes. */
    private static final int LARGO_MAXIMO_CLAVE = 72;

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
        super(repositorioUsuario);
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Usuario crearEntidad(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(normalizarCorreo(dto.nombreUsuario()));
        usuario.setRol(dto.rol());
        ProveedorAutenticacion proveedor = dto.proveedorAutenticacion() == null
                ? ProveedorAutenticacion.LOCAL : dto.proveedorAutenticacion();
        usuario.setProveedorAutenticacion(proveedor);
        if (dto.rol() != RolUsuario.CLIENTE) {
            // Regla del enunciado: todo empleado nuevo recibe la clave temporal y debe cambiarla
            usuario.setClave(CLAVE_TEMPORAL);
            usuario.setDebeCambiarClave(true);
        } else if (dto.claveTemporal() && proveedor == ProveedorAutenticacion.LOCAL) {
            // Alta en campo: la cuenta nace con la clave temporal y el cliente debe cambiarla al ingresar
            usuario.setClave(CLAVE_TEMPORAL);
            usuario.setDebeCambiarClave(true);
        } else {
            usuario.setClave(proveedor == ProveedorAutenticacion.LOCAL ? dto.clave() : null);
            usuario.setDebeCambiarClave(false);
        }
        return usuario;
    }

    @Override
    protected void actualizarEntidad(Usuario usuario, UsuarioRequestDTO dto) throws MiException {
        throw new OperacionNoPermitidaException(
                "Las cuentas no se modifican directamente: se gestionan desde empleados o clientes");
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Usuario usuario) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(usuario.getNombreUsuario())) {
            throw new MiException("Debe indicar el correo electrónico");
        }
        if (usuario.getNombreUsuario().length() > 120 || !CORREO.matcher(usuario.getNombreUsuario()).matches()) {
            throw new MiException("El correo electrónico no tiene un formato válido");
        }
        if (usuario.getRol() == null) {
            throw new MiException("Debe indicar el rol de la cuenta");
        }
        boolean esLocal = usuario.getProveedorAutenticacion() == ProveedorAutenticacion.LOCAL;
        if (usuario.getRol() != RolUsuario.CLIENTE && !esLocal) {
            throw new MiException("Los empleados solo pueden ingresar con correo y contraseña");
        }
        if (casoDeUso == BaseUseCaseService.ALTA && usuario.getRol() == RolUsuario.CLIENTE && esLocal
                && !usuario.isDebeCambiarClave()) {
            validarPoliticaClave(usuario.getClave(), usuario.getNombreUsuario());
        }
        Optional<Usuario> mismoCorreo = repositorioUsuario
                .findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(usuario.getNombreUsuario());
        if (mismoCorreo.isPresent() && !Objects.equals(mismoCorreo.get().getId(), usuario.getId())) {
            throw new MiException("Ya existe una cuenta registrada con el correo " + usuario.getNombreUsuario());
        }
    }

    /** La clave se transforma con BCrypt justo antes de guardar. */
    @Override
    protected void preAlta(Usuario usuario) {
        if (usuario.getClave() != null) {
            usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        }
    }

    @Override
    public UsuarioResponseDTO convertirAResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol(),
                usuario.isDebeCambiarClave(), usuario.getProveedorAutenticacion(), usuario.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el usuario";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    /** crearUsuario(nombreUsuario, rol) del diagrama: usa el algoritmo de alta y devuelve la cuenta. */
    @Transactional(rollbackFor = Exception.class)
    public Usuario crearUsuario(String nombreUsuario, RolUsuario rol, String clave, ProveedorAutenticacion proveedor)
            throws MiException {
        return altaEntidad(new UsuarioRequestDTO(nombreUsuario, rol, clave, proveedor));
    }

    /** Cuenta de cliente con la clave temporal y cambio obligatorio (alta desde la app del preventista). */
    @Transactional(rollbackFor = Exception.class)
    public Usuario crearCuentaClienteConClaveTemporal(String correo) throws MiException {
        return altaEntidad(new UsuarioRequestDTO(correo, RolUsuario.CLIENTE, null, ProveedorAutenticacion.LOCAL, true));
    }

    /** buscarUsuarioPorNombre(nombreUsuario) del diagrama. */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorNombre(String nombreUsuario) throws MiException {
        if (estaVacio(nombreUsuario)) {
            throw new MiException("Debe indicar el correo a buscar");
        }
        return buscarActivoPorNombreUsuario(nombreUsuario)
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta con el correo " + nombreUsuario));
    }

    /** Cuenta activa con ese correo. La usa el UserDetailsService de Spring Security. */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarActivoPorNombreUsuario(String nombreUsuario) {
        if (estaVacio(nombreUsuario)) {
            return Optional.empty();
        }
        return repositorioUsuario.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(normalizarCorreo(nombreUsuario));
    }

    /** Cualquier cuenta con ese correo (prioriza la activa). La usa el ingreso con redes sociales. */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarCualquieraPorNombreUsuario(String nombreUsuario) {
        if (estaVacio(nombreUsuario)) {
            return Optional.empty();
        }
        List<Usuario> cuentas = repositorioUsuario.findByNombreUsuarioIgnoreCase(normalizarCorreo(nombreUsuario));
        return cuentas.stream().min(Comparator.comparing(Usuario::isEliminado));
    }

    /**
     * modificarClave(id, claveActual, nuevaClave, confirmarClave) del diagrama, identificando al usuario
     * por su correo (el del token). Al cambiarla se apaga debeCambiarClave.
     */
    @Transactional(rollbackFor = Exception.class)
    public Usuario modificarClave(String nombreUsuario, String claveActual, String nuevaClave, String confirmarClave)
            throws MiException {
        Usuario usuario = buscarActivoPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta indicada"));
        if (usuario.getClave() == null) {
            throw new MiException("Tu cuenta ingresa con " + usuario.getProveedorAutenticacion()
                    + " y no tiene una contraseña para modificar");
        }
        if (estaVacio(claveActual) || !passwordEncoder.matches(claveActual, usuario.getClave())) {
            throw new MiException("La contraseña actual es incorrecta");
        }
        if (nuevaClave == null || !nuevaClave.equals(confirmarClave)) {
            throw new MiException("La nueva contraseña y su confirmación no coinciden");
        }
        validarPoliticaClave(nuevaClave, usuario.getNombreUsuario());
        if (passwordEncoder.matches(nuevaClave, usuario.getClave())) {
            throw new MiException("La nueva contraseña debe ser distinta de la actual");
        }
        usuario.setClave(passwordEncoder.encode(nuevaClave));
        usuario.setDebeCambiarClave(false);
        return repositorioUsuario.save(usuario);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Política de contraseñas: 8 a 72 caracteres, letras y números, distinta de la temporal y del correo. */
    static void validarPoliticaClave(String clave, String correo) throws MiException {
        if (clave == null || clave.isBlank()) {
            throw new MiException("Debe indicar una contraseña");
        }
        if (clave.length() < LARGO_MINIMO_CLAVE) {
            throw new MiException("La contraseña debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
        }
        if (clave.length() > LARGO_MAXIMO_CLAVE) {
            throw new MiException("La contraseña no puede superar los " + LARGO_MAXIMO_CLAVE + " caracteres");
        }
        if (!clave.matches(".*\\p{L}.*") || !clave.matches(".*\\d.*")) {
            throw new MiException("La contraseña debe combinar letras y números");
        }
        if (clave.equalsIgnoreCase(CLAVE_TEMPORAL)) {
            throw new MiException("No podés usar la contraseña temporal");
        }
        if (correo != null && clave.equalsIgnoreCase(correo)) {
            throw new MiException("La contraseña no puede ser igual al correo");
        }
    }

    static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}

package com.competencia.blockazo.service;

import com.competencia.blockazo.error.EntidadNoEncontradaException;
import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.CambioClaveRequest;
import com.competencia.blockazo.model.dto.PerfilRequest;
import com.competencia.blockazo.model.dto.RegistroRequest;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.model.enums.Rol;
import com.competencia.blockazo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio concreto del ABM de usuarios.
 * Hereda el algoritmo (alta / modificar / baja) de BaseService y
 * solo redefine los HOOKS que necesita (patron Template Method).
 */
@Service
public class UsuarioService extends BaseService<Usuario, Long> {

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PATRON_DNI = Pattern.compile("\\d{7,8}");
    private static final int LARGO_MINIMO_CLAVE = 6;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioRepository usuarioRepository() {
        return (UsuarioRepository) repository;
    }

    // =============================== HOOKS ===============================

    @Override
    protected void validar(BaseUseCaseService useCase, Usuario usuario) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return; // para la baja no hay que validar datos
        }

        normalizar(usuario);

        if (esVacio(usuario.getNombre())) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }
        if (esVacio(usuario.getApellido())) {
            throw new ErrorServiceException("Debe indicar el apellido");
        }
        if (esVacio(usuario.getCorreo())) {
            throw new ErrorServiceException("Debe indicar el correo");
        }
        if (!PATRON_CORREO.matcher(usuario.getCorreo()).matches()) {
            throw new ErrorServiceException("El correo no tiene un formato valido");
        }

        // DNI, fecha de nacimiento y direccion son obligatorios para el registro local.
        // Quien entra con Google puede completarlos despues desde el Perfil.
        boolean esLocal = usuario.getProveedor() == null || usuario.getProveedor() == Proveedor.LOCAL;
        if (esLocal || (useCase == BaseUseCaseService.MODIFICACION && usuario.getDni() != null)) {
            if (esVacio(usuario.getDni())) {
                throw new ErrorServiceException("Debe indicar el DNI");
            }
            if (usuario.getFechaNacimiento() == null) {
                throw new ErrorServiceException("Debe indicar la fecha de nacimiento");
            }
            if (esVacio(usuario.getDireccion())) {
                throw new ErrorServiceException("Debe indicar la direccion");
            }
        }
        if (usuario.getDni() != null && !PATRON_DNI.matcher(usuario.getDni()).matches()) {
            throw new ErrorServiceException("El DNI debe tener 7 u 8 digitos");
        }
        if (usuario.getFechaNacimiento() != null && !usuario.getFechaNacimiento().isBefore(LocalDate.now())) {
            throw new ErrorServiceException("La fecha de nacimiento debe ser anterior a hoy");
        }

        // Clave: obligatoria en el alta local; en la modificacion es opcional (si viene, se cambia)
        if (useCase == BaseUseCaseService.ALTA && esLocal && esVacio(usuario.getClave())) {
            throw new ErrorServiceException("Debe indicar la clave");
        }
        if (!esVacio(usuario.getClave()) && usuario.getClave().length() < LARGO_MINIMO_CLAVE) {
            throw new ErrorServiceException("La clave debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
        }

        // Unicidad de correo y DNI
        Optional<Usuario> mismoCorreo = usuarioRepository().buscarPorCorreo(usuario.getCorreo());
        if (mismoCorreo.isPresent() && !mismoCorreo.get().getId().equals(usuario.getId())) {
            throw new ErrorServiceException("Ya existe un usuario registrado con ese correo");
        }
        if (usuario.getDni() != null) {
            Optional<Usuario> mismoDni = usuarioRepository().buscarPorDni(usuario.getDni());
            if (mismoDni.isPresent() && !mismoDni.get().getId().equals(usuario.getId())) {
                throw new ErrorServiceException("Ya existe un usuario registrado con ese DNI");
            }
        }
    }

    @Override
    protected void preAlta(Usuario usuario) {
        if (!esVacio(usuario.getClave())) {
            usuario.setClave(passwordEncoder.encode(usuario.getClave()));  // nunca se guarda en texto plano
        } else {
            usuario.setClave(null);
        }
        if (usuario.getRol() == null) {
            usuario.setRol(Rol.USUARIO);
        }
        if (usuario.getProveedor() == null) {
            usuario.setProveedor(Proveedor.LOCAL);
        }
        usuario.setFechaAlta(LocalDateTime.now());
    }

    @Override
    protected void preModificacion(Usuario actual, Usuario nuevo) {
        // Datos que NO se pisan desde el formulario
        nuevo.setFechaAlta(actual.getFechaAlta());
        nuevo.setProveedor(actual.getProveedor());
        if (nuevo.getRol() == null) {
            nuevo.setRol(actual.getRol());
        }
        // Si no mandan clave, se conserva la anterior; si la mandan, se encripta la nueva
        if (esVacio(nuevo.getClave())) {
            nuevo.setClave(actual.getClave());
        } else {
            nuevo.setClave(passwordEncoder.encode(nuevo.getClave()));
        }
    }

    // ===================== CASOS DE USO PROPIOS =====================

    /** Alta de una persona desde el registro publico (register). Usa el metodo plantilla alta(). */
    @Transactional(rollbackFor = Exception.class)
    public Usuario registrar(RegistroRequest req) throws ErrorServiceException {
        if (req.clave() == null || !req.clave().equals(req.confirmacionClave())) {
            throw new ErrorServiceException("Las claves no coinciden");
        }
        Usuario usuario = Usuario.builder()
                .nombre(req.nombre())
                .apellido(req.apellido())
                .dni(req.dni())
                .fechaNacimiento(req.fechaNacimiento())
                .direccion(req.direccion())
                .correo(req.correo())
                .clave(req.clave())
                .rol(Rol.USUARIO)
                .proveedor(Proveedor.LOCAL)
                .build();
        return alta(usuario);
    }

    /**
     * Login con Google / Auth0: si el correo no existe se da de alta automaticamente
     * (sin clave y sin DNI; los completa luego desde el Perfil).
     */
    @Transactional(rollbackFor = Exception.class)
    public Usuario registrarOIngresarOAuth2(String correo, String nombre, String apellido,
                                            Proveedor proveedor) throws ErrorServiceException {
        Optional<Usuario> existente = usuarioRepository().buscarPorCorreo(correo);
        if (existente.isPresent()) {
            if (Boolean.TRUE.equals(existente.get().isEliminado())) {
                throw new ErrorServiceException("El usuario se encuentra dado de baja");
            }
            return existente.get();
        }
        Usuario usuario = Usuario.builder()
                .nombre(esVacio(nombre) ? correo.substring(0, correo.indexOf('@')) : nombre)
                .apellido(esVacio(apellido) ? "-" : apellido)
                .correo(correo)
                .rol(Rol.USUARIO)
                .proveedor(proveedor)
                .build();
        return alta(usuario);
    }

    /** "Mi perfil": datos del usuario logueado. */
    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) throws ErrorServiceException {
        return usuarioRepository().buscarPorCorreo(correo)
                .filter(u -> !Boolean.TRUE.equals(u.isEliminado()))
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe un usuario con ese correo"));
    }

    /** Seccion Perfil: modifica TODOS los datos personales (incluido el correo). Usa el metodo plantilla modificar(). */
    @Transactional(rollbackFor = Exception.class)
    public Usuario actualizarPerfil(String correoActual, PerfilRequest req) throws ErrorServiceException {
        Usuario actual = buscarPorCorreo(correoActual);
        Usuario cambios = Usuario.builder()
                .nombre(req.nombre())
                .apellido(req.apellido())
                .dni(req.dni())
                .fechaNacimiento(req.fechaNacimiento())
                .direccion(req.direccion())
                .correo(req.correo())
                .rol(actual.getRol())   // el usuario no puede cambiarse su propio rol
                .build();               // clave null -> preModificacion conserva la actual
        return modificar(actual.getId(), cambios);
    }

    /** Seccion Perfil: cambiar la clave. */
    @Transactional(rollbackFor = Exception.class)
    public void cambiarClave(String correo, CambioClaveRequest req) throws ErrorServiceException {
        Usuario usuario = buscarPorCorreo(correo);

        if (usuario.isTieneClave()) {
            if (esVacio(req.claveActual()) || !passwordEncoder.matches(req.claveActual(), usuario.getClave())) {
                throw new ErrorServiceException("La clave actual es incorrecta");
            }
            if (passwordEncoder.matches(req.claveNueva(), usuario.getClave())) {
                throw new ErrorServiceException("La nueva clave debe ser distinta a la actual");
            }
        }
        if (esVacio(req.claveNueva()) || req.claveNueva().length() < LARGO_MINIMO_CLAVE) {
            throw new ErrorServiceException("La nueva clave debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
        }
        if (!req.claveNueva().equals(req.confirmacionClave())) {
            throw new ErrorServiceException("Las claves no coinciden");
        }

        usuario.setClave(passwordEncoder.encode(req.claveNueva()));
        repository.save(usuario);
    }

    // =============================== AUXILIARES ===============================

    private void normalizar(Usuario u) {
        if (u.getCorreo() != null) u.setCorreo(u.getCorreo().trim().toLowerCase());
        if (u.getNombre() != null) u.setNombre(u.getNombre().trim());
        if (u.getApellido() != null) u.setApellido(u.getApellido().trim());
        if (u.getDireccion() != null) u.setDireccion(u.getDireccion().trim());
        if (u.getDni() != null) {
            String dni = u.getDni().replace(".", "").trim();
            u.setDni(dni.isEmpty() ? null : dni);
        }
    }

    private static boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}

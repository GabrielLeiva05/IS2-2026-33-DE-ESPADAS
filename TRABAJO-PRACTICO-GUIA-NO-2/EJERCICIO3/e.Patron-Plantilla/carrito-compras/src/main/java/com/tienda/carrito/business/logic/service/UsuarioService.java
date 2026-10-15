package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de Usuario y de ROLES:
 *  - Nombre 3 a 50 caracteres y unico. Clave 6 a 72 caracteres (obligatoria al crear; al editar, vacia = se conserva).
 *  - Clave guardada con BCrypt. El rol es obligatorio.
 *  - La autoregistracion SIEMPRE crea un CLIENTE (registrarCliente): nadie puede darse de alta como ADMIN desde afuera.
 *  - Siempre debe existir al menos un ADMIN activo: no se puede eliminar ni degradar al ultimo.
 *  - Nadie puede eliminar su propia cuenta (baja con actor).
 */
@Service
public class UsuarioService extends BaseService<Usuario, String> {

    private static final int CLAVE_MINIMA = 6;
    private static final int CLAVE_MAXIMA = 72; // limite de BCrypt

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        super(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Usuario usuario) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            if (usuario.getRol() == Rol.ADMIN && ultimoAdministrador()) {
                throw new ErrorServiceException("No se puede eliminar al único administrador del sistema.");
            }
            return;
        }

        if (vacio(usuario.getNombre())) {
            throw new ErrorServiceException("El nombre de usuario es obligatorio.");
        }
        String nombre = usuario.getNombre().trim();
        if (nombre.length() < 3 || nombre.length() > 50) {
            throw new ErrorServiceException("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }
        Usuario existente = usuarioRepository.buscarUsuarioPorNombre(nombre);
        if (existente != null && !existente.getId().equals(usuario.getId())) {
            throw new ErrorServiceException("Ya existe un usuario con el nombre \"" + nombre + "\".");
        }
        if (useCase == BaseUseCaseService.ALTA || !vacio(usuario.getClave())) {
            validarClave(usuario.getClave());
        }
        if (usuario.getRol() == null) {
            throw new ErrorServiceException("Debe indicar el rol del usuario.");
        }
        if (useCase == BaseUseCaseService.MODIFICACION) {
            Usuario actual = usuarioRepository.findById(usuario.getId()).orElse(null);
            boolean seDegrada = actual != null && actual.getRol() == Rol.ADMIN && usuario.getRol() != Rol.ADMIN;
            if (seDegrada && ultimoAdministrador()) {
                throw new ErrorServiceException("Debe existir al menos un administrador: no se puede quitar el rol al único que hay.");
            }
        }
    }

    private void validarClave(String clave) throws ErrorServiceException {
        if (vacio(clave)) {
            throw new ErrorServiceException("La clave es obligatoria.");
        }
        if (clave.length() < CLAVE_MINIMA || clave.length() > CLAVE_MAXIMA) {
            throw new ErrorServiceException("La clave debe tener entre " + CLAVE_MINIMA + " y " + CLAVE_MAXIMA + " caracteres.");
        }
    }

    private boolean ultimoAdministrador() {
        return usuarioRepository.contarUsuariosActivosPorRol(Rol.ADMIN) <= 1;
    }

    @Override
    protected void preAlta(Usuario usuario) {
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
    }

    /** En modificar() el usuario recibido reemplaza al guardado: si no escribieron clave nueva, se conserva el hash actual. */
    @Override
    protected void preModificacion(Usuario usuarioNuevo) throws ErrorServiceException {
        Usuario actual = obtenerEntidad(usuarioNuevo.getId());
        usuarioNuevo.setNombre(usuarioNuevo.getNombre().trim());
        if (vacio(usuarioNuevo.getClave())) {
            usuarioNuevo.setClave(actual.getClave());
        } else {
            usuarioNuevo.setClave(passwordEncoder.encode(usuarioNuevo.getClave()));
        }
    }

    // ===================== Casos de uso propios de Usuario =====================

    /** Registro publico: el usuario creado es siempre CLIENTE, sin importar lo que llegue desde el formulario. */
    @Transactional(rollbackFor = Exception.class)
    public Usuario registrarCliente(Usuario usuario) throws ErrorServiceException {
        usuario.setRol(Rol.CLIENTE);
        return alta(usuario);
    }

    /** Baja de un usuario hecha por otro usuario (el "actor"): nadie puede eliminarse a si mismo. */
    @Transactional(rollbackFor = Exception.class)
    public void baja(String idUsuario, String idActor) throws ErrorServiceException {
        if (idUsuario != null && idUsuario.equals(idActor)) {
            throw new ErrorServiceException("No podés eliminar tu propia cuenta.");
        }
        baja(idUsuario);
    }

    /** Verifica nombre y clave. Devuelve el usuario o lanza ErrorServiceException con un mensaje generico. */
    @Transactional(readOnly = true)
    public Usuario autenticar(String nombre, String clave) throws ErrorServiceException {
        if (vacio(nombre) || vacio(clave)) {
            throw new ErrorServiceException("Ingresá usuario y clave.");
        }
        Usuario usuario = usuarioRepository.buscarUsuarioPorNombre(nombre.trim());
        if (usuario == null || !passwordEncoder.matches(clave, usuario.getClave())) {
            throw new ErrorServiceException("Usuario o clave incorrectos.");
        }
        return usuario;
    }

    /**
     * Se usa al iniciar la aplicacion: garantiza que exista al menos un ADMIN activo.
     * Si ya existe uno no hace nada. Si no, promueve al usuario con ese nombre (caso base migrada desde la
     * version sin roles) o lo crea.
     */
    @Transactional(rollbackFor = Exception.class)
    public void asegurarAdministrador(String nombre, String claveInicial) throws ErrorServiceException {
        if (usuarioRepository.contarUsuariosActivosPorRol(Rol.ADMIN) > 0) {
            return;
        }
        Usuario existente = usuarioRepository.buscarUsuarioPorNombre(nombre);
        if (existente != null) {
            existente.setRol(Rol.ADMIN);
            usuarioRepository.save(existente);
        } else {
            Usuario admin = new Usuario();
            admin.setNombre(nombre);
            admin.setClave(claveInicial);
            admin.setRol(Rol.ADMIN);
            alta(admin);
        }
    }
}

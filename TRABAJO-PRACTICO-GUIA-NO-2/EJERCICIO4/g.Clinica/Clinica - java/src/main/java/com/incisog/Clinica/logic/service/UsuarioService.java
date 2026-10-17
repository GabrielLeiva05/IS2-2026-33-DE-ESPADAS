package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.UsuarioRepository;
import com.incisog.Clinica.models.Usuario;
import org.springframework.stereotype.Service;

/**
 * Hereda de la plantilla: listar, buscarPorId, crear, modificar, eliminar.
 * Particularidad: la clave es obligatoria al CREAR pero opcional al MODIFICAR,
 * por eso no va en validar() (que corre en los dos casos) sino en antesDeCrear().
 */
@Service
public class UsuarioService extends BaseServiceTemplate<Usuario> {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository repository) {
        super(repository);
        this.usuarioRepository = repository;
    }

    // ---------- pasos de la plantilla ----------

    @Override
    protected void validar(Usuario usuario) throws ErrorServiceException {
        if (usuario == null) {
            throw new ErrorServiceException("Debe indicar el usuario");
        }
        if (esVacio(usuario.getNombreUsuario())) {
            throw new ErrorServiceException("Debe indicar el nombre de usuario");
        }
    }

    @Override
    protected void antesDeCrear(Usuario usuario) throws ErrorServiceException {
        if (esVacio(usuario.getClave())) {
            throw new ErrorServiceException("Debe indicar la clave");
        }
        chequearNombreUsuario(usuario.getNombreUsuario());
    }

    @Override
    protected void antesDeModificar(Usuario existente, Usuario datos) throws ErrorServiceException {
        if (!existente.getNombreUsuario().equals(datos.getNombreUsuario())) {
            chequearNombreUsuario(datos.getNombreUsuario());
        }
    }

    @Override
    protected void copiarDatos(Usuario existente, Usuario datos) {
        existente.setNombreUsuario(datos.getNombreUsuario());
        // La API nunca devuelve la clave, así que el frontend no puede reenviarla:
        // si viene vacía se conserva la anterior; si viene con valor, se cambia.
        if (!esVacio(datos.getClave())) {
            existente.setClave(datos.getClave());
        }
    }

    // ---------- métodos propios de Usuario (fuera de la plantilla) ----------

    /**
     * Login simple: mismo mensaje si falla el usuario o la clave,
     * para no revelar cuál de los dos existe.
     */
    public Usuario login(String nombreUsuario, String clave) throws ErrorServiceException {
        if (esVacio(nombreUsuario) || esVacio(clave)) {
            throw new ErrorServiceException("Debe indicar usuario y clave");
        }
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(nombreUsuario)
                .orElseThrow(() -> new ErrorServiceException("Usuario o clave incorrectos"));
        if (!usuario.getClave().equals(clave)) {
            throw new ErrorServiceException("Usuario o clave incorrectos");
        }
        return usuario;
    }

    // ---------- auxiliares ----------

    private void chequearNombreUsuario(String nombreUsuario) throws ErrorServiceException {
        if (usuarioRepository.existsByNombreUsuarioAndEliminadoFalse(nombreUsuario)) {
            throw new ErrorServiceException("Ya existe un usuario con ese nombre");
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}

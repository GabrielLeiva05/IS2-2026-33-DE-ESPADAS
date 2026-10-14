package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.UsuarioRepository;
import com.incisog.Clinica.models.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listar() {
        return repository.findByEliminadoFalse();
    }

    public Usuario buscarPorId(String id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(usuario -> !usuario.isEliminado())
                .orElseThrow(() -> new ErrorServiceException("Usuario no encontrado o eliminado."));
    }

    public Usuario crear(Usuario usuario) throws ErrorServiceException {
        validar(usuario);
        if (esVacio(usuario.getClave())) {
            throw new ErrorServiceException("Debe indicar la clave");
        }
        chequearNombreUsuario(usuario.getNombreUsuario());
        usuario.setId(null);
        usuario.setEliminado(false);
        return repository.save(usuario);
    }

    public Usuario modificar(String id, Usuario datos) throws ErrorServiceException {
        validar(datos);
        Usuario usuario = buscarPorId(id);

        if (!usuario.getNombreUsuario().equals(datos.getNombreUsuario())) {
            chequearNombreUsuario(datos.getNombreUsuario());
        }
        usuario.setNombreUsuario(datos.getNombreUsuario());

        // La API nunca devuelve la clave, así que el frontend no la tiene para reenviarla.
        // Regla: si viene vacía, se conserva la anterior; si viene con valor, se cambia.
        if (!esVacio(datos.getClave())) {
            usuario.setClave(datos.getClave());
        }
        return repository.save(usuario);
    }

    public void eliminar(String id) throws ErrorServiceException {
        Usuario usuario = buscarPorId(id);
        usuario.setEliminado(true);
        repository.save(usuario);
    }

    /**
     * Login simple: busca el usuario activo por nombre y compara la clave.
     * El mensaje es el mismo si falla el usuario o la clave, para no revelar
     * cuál de los dos existe.
     */
    public Usuario login(String nombreUsuario, String clave) throws ErrorServiceException {
        if (esVacio(nombreUsuario) || esVacio(clave)) {
            throw new ErrorServiceException("Debe indicar usuario y clave");
        }
        Usuario usuario = repository.findByNombreUsuarioAndEliminadoFalse(nombreUsuario)
                .orElseThrow(() -> new ErrorServiceException("Usuario o clave incorrectos"));
        if (!usuario.getClave().equals(clave)) {
            throw new ErrorServiceException("Usuario o clave incorrectos");
        }
        return usuario;
    }

    private void validar(Usuario usuario) throws ErrorServiceException {
        if (usuario == null) {
            throw new ErrorServiceException("Debe indicar el usuario");
        }
        if (esVacio(usuario.getNombreUsuario())) {
            throw new ErrorServiceException("Debe indicar el nombre de usuario");
        }
    }

    private void chequearNombreUsuario(String nombreUsuario) throws ErrorServiceException {
        if (repository.existsByNombreUsuarioAndEliminadoFalse(nombreUsuario)) {
            throw new ErrorServiceException("Ya existe un usuario con ese nombre");
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

}

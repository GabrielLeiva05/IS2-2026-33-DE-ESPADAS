package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServicio implements UserDetailsService {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    @Lazy
    private PersonaServicio personaServicio;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private ServicioContactoTelefonico servicioContactoTelefonico;

    @Transactional
    public void registrar(Direccion direccion,
                          String documento,
                          TipoDocumento tipoDocumento,
                          String nombre,
                          String apellido,
                          String email,
                          String clave,
                          String clave2,
                          Date fechaNacimiento,
                          String sexo,
                          String telefono) throws MiException {

        validar(documento, tipoDocumento, nombre, apellido, email, clave, clave2, fechaNacimiento);

        Persona persona = personaServicio.crearPersona(direccion, documento, tipoDocumento, nombre,
            fechaNacimiento, apellido, sexo);
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setNombreUsuario(email);
        usuario.setClave(passwordEncoder.encode(clave));
        usuario.setRolUsuario(RolUsuario.CLIENTE);

        /*
        Imagen imagen = imagenServicio.guardar(archivo);
        usuario.setImagen(imagen);*/

        usuarioRepositorio.save(usuario);
        servicioContactoTelefonico.crearContactoTelefonico(telefono, TipoTelefono.CELULAR,
            TipoContacto.PERSONAL, null, persona.getId(), null);
    }

    private void validar(String documento,
                         TipoDocumento tipoDocumento,
                         String nombre,
                         String apellido,
                         String email,
                         String clave,
                         String clave2,
                         Date fechaNacimiento) throws MiException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre no puede estar vacío");
        }
        if (nombre.length() < 3) {
            throw new MiException("El nombre debe tener más caracteres");
        }
        if (documento == null || documento.length() >9 &&  tipoDocumento.toString().equals("DNI") || documento.length()<6 && tipoDocumento.toString().equals("DNI") ) {
            throw new MiException("El documento es inválido");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new MiException("El nombre no puede estar vacío");
        }
        if (apellido.length() < 3) {
            throw new MiException("El apellido debe tener más caracteres");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new MiException("El email no puede estar vacío");
        }
        if (clave == null || clave.length() <= 5) {
            throw new MiException("La contraseña debe tener más de 5 caracteres");
        }
        if (!clave.equals(clave2)) {
            throw new MiException("Las contraseñas ingresadas deben ser iguales");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);

        if (usuario != null && !usuario.isEliminado()) {
            List<GrantedAuthority> permisos = new ArrayList<>();
            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + usuario.getRolUsuario().name());
            permisos.add(p);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", usuario);

            return new org.springframework.security.core.userdetails.User(
                    usuario.getNombreUsuario(),
                    usuario.getClave(),
                    permisos
            );
        }

        throw new UsernameNotFoundException("Usuario no encontrado con el email: " + nombreUsuario);

    }

    @Transactional
    public Usuario getById(String id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String id) throws MiException {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe un usuario con id: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarActivoPorNombreUsuario(String nombreUsuario) throws MiException {
        Usuario usuario = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);
        if (usuario == null || usuario.isEliminado()) {
            throw new MiException("No existe un usuario activo con el email indicado");
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarActivoPorPersonaId(String personaId) {
        return usuarioRepositorio.findByPersonaIdAndEliminadoFalse(personaId);
    }

    @Transactional
    public void desactivarUsuarioActivoDePersona(String personaId, String idUsuarioNuevo) {
        buscarActivoPorPersonaId(personaId).ifPresent(usuarioAnterior -> {
            if (!usuarioAnterior.getId().equals(idUsuarioNuevo)) {
                usuarioAnterior.setEliminado(true);
                usuarioRepositorio.save(usuarioAnterior);
            }
        });
    }

    @Transactional
    public void desactivarTodosLosUsuariosDePersona(Persona persona) {
        if (persona.getUsuarios() != null) {
            for (Usuario u : persona.getUsuarios()) {
                if (!u.isEliminado()) {
                    u.setEliminado(true);
                    usuarioRepositorio.save(u);
                }
            }
        }
    }

    @Transactional
    public void asociarAPersona(Usuario usuario, Persona persona) {
        usuario.setPersona(persona);
        usuario.setEliminado(false);
        usuarioRepositorio.save(usuario);
    }

    public List<String> listarCorreosClientesActivos() {
        return usuarioRepositorio.findByRolUsuarioAndEliminadoFalse(RolUsuario.CLIENTE).stream()
                .map(Usuario::getNombreUsuario)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Transactional
    public Usuario crearUsuarioAdmin(String nombreUsuario, String clave, RolUsuario rolUsuario, String idPersona)
            throws MiException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new MiException("El nombre de usuario no puede estar vacío");
        }
        if (clave == null || clave.length() <= 5) {
            throw new MiException("La contraseña debe tener más de 5 caracteres");
        }
        if (rolUsuario == null) {
            throw new MiException("Debe indicar el rol del usuario");
        }
        Persona persona = personaServicio.buscarPersona(idPersona);

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setClave(passwordEncoder.encode(clave));
        usuario.setRolUsuario(rolUsuario);
        usuario.setPersona(persona);
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Usuario modificarUsuarioAdmin(String id, String nombreUsuario, String clave, RolUsuario rolUsuario,
            String idPersona) throws MiException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new MiException("El nombre de usuario no puede estar vacío");
        }
        if (rolUsuario == null) {
            throw new MiException("Debe indicar el rol del usuario");
        }
        Usuario usuario = buscarPorId(id);
        Persona persona = personaServicio.buscarPersona(idPersona);

        usuario.setNombreUsuario(nombreUsuario.trim());
        if (clave != null && !clave.isBlank()) {
            if (clave.length() <= 5) {
                throw new MiException("La contraseña debe tener más de 5 caracteres");
            }
            usuario.setClave(passwordEncoder.encode(clave));
        }
        usuario.setRolUsuario(rolUsuario);
        usuario.setPersona(persona);
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(String id) throws MiException {
        Usuario usuario = buscarPorId(id);
        usuario.setEliminado(true);
        usuarioRepositorio.save(usuario);
    }
}




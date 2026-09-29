package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioServicio implements UserDetailsService {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PersonaServicio personaServicio;



    public void registrar(Direccion direccion,
                          String documento,
                          TipoDocumento tipoDocumento,
                          String nombre,
                          String apellido,
                          String email,
                          String clave,
                          String clave2,
                          Date fechaNacimiento) throws MiException {

        validar(documento, tipoDocumento, nombre, apellido, email, clave, clave2, fechaNacimiento);

        Persona persona = personaServicio.crearPersona(direccion, documento, tipoDocumento, nombre, fechaNacimiento, apellido);
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setNombreUsuario(email);
        usuario.setClave(new BCryptPasswordEncoder().encode(clave));
        usuario.setRolUsuario(RolUsuario.CLIENTE);

        /*
        Imagen imagen = imagenServicio.guardar(archivo);
        usuario.setImagen(imagen);*/

        usuarioRepositorio.save(usuario);
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
        } else {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + nombreUsuario);
        }

    }

    @Transactional
    public Usuario getById(String id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }

    // ---------- Métodos usados por el ABM del dashboard ----------

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @Transactional
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    @Transactional
    public Usuario buscarPorId(String id) throws MiException {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró el usuario solicitado"));
    }

    @Transactional
    public Usuario crearUsuario(String nombreUsuario, String clave, String clave2,
                                RolUsuario rolUsuario, Persona persona) throws MiException {
        validarUsuario(nombreUsuario, rolUsuario);
        if (persona == null) {
            throw new MiException("Debe asociar el usuario a una persona");
        }
        validarClave(clave, clave2, true);
        validarNombreUsuarioUnico(nombreUsuario.trim(), null);
        if (usuarioRepositorio.findByPersonaId(persona.getId()).isPresent()) {
            throw new MiException("La persona seleccionada ya tiene un usuario asociado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setClave(new BCryptPasswordEncoder().encode(clave));
        usuario.setRolUsuario(rolUsuario);
        usuario.setPersona(persona);
        usuario.setEliminado(false);
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Usuario modificarUsuario(String id, String nombreUsuario, String clave, String clave2,
                                    RolUsuario rolUsuario) throws MiException {
        validarUsuario(nombreUsuario, rolUsuario);
        Usuario usuario = buscarPorId(id);
        validarNombreUsuarioUnico(nombreUsuario.trim(), id);
        // La clave es opcional al modificar: si viene vacía se conserva la actual.
        if (clave != null && !clave.isEmpty()) {
            validarClave(clave, clave2, false);
            usuario.setClave(new BCryptPasswordEncoder().encode(clave));
        }
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setRolUsuario(rolUsuario);
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(String id) throws MiException {
        Usuario usuario = buscarPorId(id);
        usuario.setEliminado(true);
        usuarioRepositorio.save(usuario);
    }

    private void validarUsuario(String nombreUsuario, RolUsuario rolUsuario) throws MiException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new MiException("El email del usuario no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(nombreUsuario.trim()).matches()) {
            throw new MiException("El email ingresado no es válido");
        }
        if (rolUsuario == null) {
            throw new MiException("Debe seleccionar un rol");
        }
    }

    private void validarClave(String clave, String clave2, boolean obligatoria) throws MiException {
        if (clave == null || clave.length() <= 5) {
            throw new MiException("La contraseña debe tener más de 5 caracteres");
        }
        if (!clave.equals(clave2)) {
            throw new MiException("Las contraseñas ingresadas deben ser iguales");
        }
    }

    private void validarNombreUsuarioUnico(String nombreUsuario, String idActual) throws MiException {
        Usuario existente = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);
        if (existente != null && (idActual == null || !existente.getId().equals(idActual))) {
            throw new MiException("Ya existe un usuario con el email: " + nombreUsuario);
        }
    }
}

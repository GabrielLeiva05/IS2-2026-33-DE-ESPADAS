package com.example.securityDemo.servicio;

import com.example.securityDemo.entidades.Imagen;
import com.example.securityDemo.entidades.Usuario;
import com.example.securityDemo.enumeraciones.Rol;
import com.example.securityDemo.exceptions.MiException;
import com.example.securityDemo.repositorio.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.internal.util.collections.ReadOnlyMap;
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
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioServicio implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ImagenServicio imagenServicio;

    @Transactional
    public void registrar(MultipartFile archivo, Long dni, String nombre, String telefono, String email, String clave, String clave2) throws MiException {

        validar(nombre, email, clave, clave2);

        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setNombre(nombre);
        usuario.setTelefono(telefono);
        usuario.setEmail(email);
        usuario.setClave(new BCryptPasswordEncoder().encode(clave));
        usuario.setRol(Rol.USER);
        usuario.setAlta(true);

        Imagen imagen = imagenServicio.guardar(archivo);
        usuario.setImagen(imagen);

        usuarioRepositorio.save(usuario);
    }

@Transactional
    public void actualizar(MultipartFile archivo, Long idUsuario, String nombre, String email, String clave, String clave2) throws MiException {

        validarDatosBasicos(nombre, email);

        Optional<Usuario> respuesta = usuarioRepositorio.findById(idUsuario);
        if (respuesta.isPresent()) {

            Usuario usuario = respuesta.get();
            usuario.setNombre(nombre);
            usuario.setEmail(email);

            // Solo validamos y modificamos la contraseña si el usuario escribió algo
            if (clave != null && !clave.trim().isEmpty()) {
                validarClave(clave, clave2);
                usuario.setClave(new BCryptPasswordEncoder().encode(clave));
            }

            Long idImagen = null;
            if (usuario.getImagen() != null) {
                idImagen = usuario.getImagen().getId();
            }
            Imagen imagen = imagenServicio.actualizar(archivo, idImagen);
            usuario.setImagen(imagen);

            usuarioRepositorio.save(usuario);
            
            // Actualizamos la sesión activa para reflejar los cambios en tiempo real
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false);
                if (session != null) {
                    session.setAttribute("usuariosession", usuario);
                }
            }
        }
    }

    private void validarDatosBasicos(String nombre, String email) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new MiException("El email no puede estar vacío");
        }
    }

    private void validarClave(String clave, String clave2) throws MiException {
        if (clave == null || clave.trim().isEmpty() || clave.length() <= 5) {
            throw new MiException("La contraseña debe tener más de 5 caracteres");
        }
        if (!clave.equals(clave2)) {
            throw new MiException("Las contraseñas ingresadas deben ser iguales");
        }
    }

    private void validar(String nombre, String email, String clave, String clave2) throws MiException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new MiException("El email no puede estar vacío");
        }
        if (clave == null || clave.isEmpty() || clave.length() <= 5) {
            throw new MiException("La contraseña debe tener más de 5 caracteres");
        }
        if (!clave.equals(clave2)) {
            throw new MiException("Las contraseñas ingresadas deben ser iguales");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);

        if (usuario != null) {
            List<GrantedAuthority> permisos = new ArrayList<>();
            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
            permisos.add(p);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", usuario);

            return new org.springframework.security.core.userdetails.User(
                    usuario.getEmail(),
                    usuario.getClave(),
                    permisos
            );
        } else {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }

    }

    @Transactional
    public Usuario getById(Long id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }
}

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

        if (usuario != null) {
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
}




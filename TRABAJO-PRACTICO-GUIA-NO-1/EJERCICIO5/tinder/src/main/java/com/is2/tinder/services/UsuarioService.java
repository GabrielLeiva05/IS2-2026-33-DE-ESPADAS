package com.is2.tinder.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.entities.Foto;
import com.is2.tinder.entities.Usuario;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.repositories.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final FotoService fotoService;
    private final NotificacionService notificationService;

    public UsuarioService(UsuarioRepository usuarioRepository, FotoService fotoService, NotificacionService notificationService) {
        this.usuarioRepository = usuarioRepository;
        this.fotoService = fotoService;
        this.notificationService = notificationService;
    }

    public void registrar(MultipartFile archivo, String nombre, String apellido, String mail, String clave) throws ErrorService {
        validar(nombre, apellido, mail, clave);
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setMail(mail);

        String encriptada = new BCryptPasswordEncoder().encode(clave);
        usuario.setClave(encriptada);

        usuario.setAlta(LocalDateTime.now());

        Foto foto = fotoService.guardar(archivo);
        usuario.setFoto(foto);

        usuarioRepository.save(usuario);

        notificationService.enviar("Bienvenidos", "Tinder", usuario.getMail());
    }

    public void modificar(MultipartFile archivo, String id, String nombre, String apellido, String mail, String clave) throws ErrorService {
        validar(nombre, apellido, mail, clave);

        Optional<Usuario> respuesta = usuarioRepository.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = usuarioRepository.findById(id).get();
            usuario.setApellido(apellido);
            usuario.setNombre(nombre);
            usuario.setMail(mail);

            String encriptada = new BCryptPasswordEncoder().encode(clave);
            usuario.setClave(encriptada);

            String idFoto = null;
            if (usuario.getFoto() != null) {
                idFoto = usuario.getFoto().getId();
            }
            Foto foto = fotoService.actualizar(idFoto, archivo);
            usuario.setFoto(foto);

            usuarioRepository.save(usuario);
        } else {
            throw new ErrorService("No se encontró el usuario solicitado");
        }
    }

    public void deshabilitar(String id) throws ErrorService {
        Optional<Usuario> respuesta = usuarioRepository.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = usuarioRepository.findById(id).get();
            usuario.setBaja(LocalDateTime.now());
            usuarioRepository.save(usuario);
        } else {
            throw new ErrorService("No se encontró el usuario solicitado");
        }
    }

    public void habilitar(String id) throws ErrorService {
        Optional<Usuario> respuesta = usuarioRepository.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = usuarioRepository.findById(id).get();
            usuario.setBaja(null);
            usuarioRepository.save(usuario);
        } else {
            throw new ErrorService("No se encontró el usuario solicitado");
        }
    }

    public void validar(String nombre, String apellido, String mail, String clave) throws ErrorService {
        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorService("El nombre del usuario no puede ser nulo");
        }
        if (apellido == null || apellido.isEmpty()) {
            throw new ErrorService("El apellido del usuario no puede ser nulo");
        }
        if (mail == null || mail.isEmpty()) {
            throw new ErrorService("El mail del usuario no puede ser nulo");
        }
        if (clave == null || clave.isEmpty() || clave.length() < 6) {
            throw new ErrorService("La clave del usuario no puede ser nula y tiene que tener más de 6 dígitos");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorMail(mail);

        if (usuario != null) {
            List<GrantedAuthority> permisos = new ArrayList<>();

            GrantedAuthority p1 = new SimpleGrantedAuthority("MODULO_FOTOS");
            permisos.add(p1);

            GrantedAuthority p2 = new SimpleGrantedAuthority("MODULO_MASCOTAS");
            permisos.add(p2);

            GrantedAuthority p3 = new SimpleGrantedAuthority("MODULO_VOTOS");
            permisos.add(p3);

            User user = new User(usuario.getMail(), usuario.getClave(), permisos);
            return user;
        } else {
            return null;
        }
    }
}

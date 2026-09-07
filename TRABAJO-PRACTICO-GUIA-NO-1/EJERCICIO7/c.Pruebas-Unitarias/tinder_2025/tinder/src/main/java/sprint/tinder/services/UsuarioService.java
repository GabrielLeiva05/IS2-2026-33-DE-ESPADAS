package sprint.tinder.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sprint.tinder.entities.Foto;
import sprint.tinder.entities.Usuario;
import sprint.tinder.entities.Zona;
import sprint.tinder.enumerations.Rol;
import sprint.tinder.errors.ErrorServicio;
import sprint.tinder.repositories.UsuarioRepository;
import sprint.tinder.repositories.ZonaRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired //Le indica que lo tiene que inicializar
    private UsuarioRepository usuarioRepository;
    @Autowired
    private FotoService fotoService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ZonaRepository zonaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Bean definido en SecurityConfig (BCrypt)

    @Transactional
    public void registrar(String nombre, String apellido, String mail, String clave, String clave2, MultipartFile archivo, String idZona) throws ErrorServicio { // hay que indicar que puede largar errores de ese tipo cpm throws ErrorServicio
        Optional<Zona> zonaOpt = zonaRepository.findById(idZona); //getOne() del video estaba deprecado
        if (!zonaOpt.isPresent()) {
            throw new ErrorServicio("No se encontró la zona solicitada");
        }
        Zona zona = zonaOpt.get();

        validar(nombre, apellido, mail, clave, clave2, zona);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setMail(mail);
        String encriptada = passwordEncoder.encode(clave);
        usuario.setClave(encriptada); // la contraseña se persiste encriptada (BCrypt), nunca en texto plano
        usuario.setRol(Rol.USUARIO); // todo alta nueva entra como usuario común; el rol ADMIN se otorga aparte
        usuario.setZona(zona);
        usuario.setAlta(new Date());

        Foto foto = fotoService.guardar(archivo);
        usuario.setFoto(foto);

        usuarioRepository.save(usuario);
        //Nosotros recibimos los datos por un formulario web, se transforman en tipo usuario y cuando se transforman le decimos al repositorio que lo almacene en la base de datos

         // Mensaje de bienvenida al mail
        try {
            notificationService.enviarMail("¡Bienvenido al Tinder de mascotas!", "Tinder de Mascotas", usuario.getMail());
        } catch (Exception ex) {
            // loguear pero no relanzar para no afectar la transacción ya completada
            Logger.getLogger(UsuarioService.class.getName()).log(Level.SEVERE, "Error enviando notificación: " + ex.getMessage(), ex);
        }

    }

    @Transactional
    public void modificar(String id, String nombre, String apellido, String mail, String clave,String clave2, MultipartFile archivo, String idZona) throws ErrorServicio {
        Optional<Zona> zonaOpt = zonaRepository.findById(idZona); //getOne() del video estaba deprecado
        if (!zonaOpt.isPresent()) {
            throw new ErrorServicio("No se encontró la zona solicitada");
        }
        Zona zona = zonaOpt.get();

        validar(nombre, apellido, mail, clave, clave2, zona);
        Optional<Usuario> respuesta = this.usuarioRepository.findById(id); // Con optional puedo ver si lo que devolvio la base de datos está presente (o sea ese usuario como respuesta a ese id)
        if(respuesta.isPresent()){ //Si me devuelve la base de datos un usuario con ese id
            Usuario usuario = respuesta.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setMail(mail);
            String encriptada = passwordEncoder.encode(clave);
            usuario.setClave(encriptada); // se vuelve a encriptar la nueva clave elegida
            usuario.setZona(zona);

            String idFoto = null;
            if(usuario.getFoto() != null){ //Me fijo si tenia una foto antes
                idFoto = usuario.getFoto().getId();
            }
            Foto foto = fotoService.actualizar(idFoto, archivo);
            usuario.setFoto(foto);
            usuarioRepository.save(usuario);
        } else{
            throw new ErrorServicio("No se encontró el usuario solicitado");
        }
    }
    public Usuario login(String email, String clave) throws ErrorServicio {

        try {

            if (email == null || email.trim().isEmpty()) {
                throw new ErrorServicio("Debe indicar el usuario");
            }
            if (clave == null || clave.trim().isEmpty()) {
                throw new ErrorServicio("Debe indicar la clave");
            }

            Usuario usuario = usuarioRepository.buscarPorMail(email);
            // Comparamos la clave ingresada (texto plano) contra el hash BCrypt guardado.
            // Nunca se descifra la clave: matches() aplica el mismo hash y compara resultados.
            if (usuario == null || !passwordEncoder.matches(clave, usuario.getClave())) {
                throw new ErrorServicio("No existe usuario con ese correo y clave");
            }
            if (usuario.isEliminado() || usuario.getBaja() != null) {
                throw new ErrorServicio("El usuario se encuentra deshabilitado");
            }
            return usuario;

        }catch(ErrorServicio e) {
            throw e;
        }catch(Exception e) {
            e.printStackTrace();
            throw new ErrorServicio("Error de Sistemas");
        }
    }

    @Transactional
    public void deshabilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta = usuarioRepository.findById(id); // Con optional puedo ver si lo que devolvio la base de datos está presente (o sea ese usuario como respuesta a ese id)
        if(respuesta.isPresent()){ //Si me devuelve la base de datos un usuario con ese id
            Usuario usuario = respuesta.get();
            usuario.setBaja(new Date());
            usuarioRepository.save(usuario);
        } else{
            throw new ErrorServicio("No se encontró el usuario solicitado");
        }
    }

    @Transactional
    public void habilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta = usuarioRepository.findById(id); // Con optional puedo ver si lo que devolvio la base de datos está presente (o sea ese usuario como respuesta a ese id)
        if(respuesta.isPresent()){ //Si me devuelve la base de datos un usuario con ese id
            Usuario usuario = respuesta.get();
            usuario.setBaja(null);
            usuarioRepository.save(usuario);
        } else{
            throw new ErrorServicio("No se encontró el usuario solicitado");
        }
    }

    public void validar(String nombre,  String apellido, String mail, String clave, String clave2, Zona zona) throws ErrorServicio {
        if (nombre == null || nombre.isEmpty()) {// Antes de persistir, tenemos que validar
            throw new ErrorServicio("El nombre es obligatorio");
        }
        if (apellido == null || apellido.isEmpty()) {// Antes de persistir, tenemos que validar
            throw new ErrorServicio("El apellido es obligatorio");
        }
        if (mail == null || mail.isEmpty()) {// Antes de persistir, tenemos que validar
            throw new ErrorServicio("El mail es obligatorio");
        }
        if (clave == null || clave.isEmpty() || clave.length() <= 5) {
            throw new ErrorServicio("La clave es obligatoria y debe tener más de 5 caracteres");
        }
        if (clave2==null || clave2.isEmpty() || clave2.length() <= 5) {
            throw new ErrorServicio("Debe verificar la clave");
        }
        if (!clave.equals(clave2)) {
            throw new ErrorServicio("Las claves deben coincidir");
        }
        if (zona == null){
            throw new ErrorServicio("No se encontró la zona solicitada");
        }
    }

    @Transactional(readOnly=true)
    public Usuario buscarUsuario(String idUsuario) throws ErrorServicio {
        try {
            if (idUsuario == null || idUsuario.trim().isEmpty()) {
                throw new ErrorServicio("Debe indicar el usuario");
            }
            Optional<Usuario> optional = usuarioRepository.findById(idUsuario);
            Usuario usuario = null;
            if (optional.isPresent()) {
                usuario= optional.get();
                if (usuario.isEliminado()){
                    throw new ErrorServicio("No se encuentra el usuario indicado");
                }
            }
            return usuario;

        } catch (ErrorServicio e) {
            throw e;
        }
    }


    @Transactional(readOnly=true)
    public List<Usuario> listarUsuario()throws ErrorServicio {

        try {

            return usuarioRepository.findAll();

        }catch(Exception e) {
            e.printStackTrace();
            throw new ErrorServicio("Error de Sistemas");
        }

    }
    // Puente entre nuestro Usuario y el modelo de autenticación de Spring Security.
    // Spring Security llama a este método al validar el login (formLogin) contra
    // el mail ingresado. Devolvemos un UserDetails con la clave YA encriptada
    // (Spring Security se encarga de compararla con el PasswordEncoder configurado)
    // y con el authority correspondiente al rol del usuario (ROLE_USUARIO o ROLE_ADMIN).
    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = this.usuarioRepository.buscarPorMail(mail);
        if (usuario == null) {
            throw new UsernameNotFoundException("No existe un usuario con ese mail");
        }
        if (usuario.isEliminado() || usuario.getBaja() != null) {
            throw new UsernameNotFoundException("El usuario se encuentra deshabilitado");
        }

        List<GrantedAuthority> permisos = new ArrayList<>();
        Rol rol = usuario.getRol() != null ? usuario.getRol() : Rol.USUARIO; // por compatibilidad con datos viejos
        permisos.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));

        // Mantenemos el atributo de sesión "usuariosession" que ya usan todos los
        // controllers y templates existentes, para no tener que reescribir esa parte.
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", usuario);
        } catch (IllegalStateException ex) {
            // No hay request HTTP activo (ej: llamado fuera de un login web); lo ignoramos.
        }

        return new User(usuario.getMail(), usuario.getClave(), permisos);
    }

}

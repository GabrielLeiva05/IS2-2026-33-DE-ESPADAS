package sprint.tinder.config;

import org.springframework.security.core.context.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import sprint.tinder.entities.Usuario;
import sprint.tinder.enumerations.Rol;
import sprint.tinder.repositories.UsuarioRepository;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public OAuth2LoginSuccessHandler(UsuarioRepository usuarioRepository,
                                     PasswordEncoder passwordEncoder,
                                     UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String mail = oauthUser.getAttribute("email");
        String nombre = oauthUser.getAttribute("name");
        Boolean verificado = oauthUser.getAttribute("email_verified");

        // Solo aceptamos mails que Google haya verificado
        if (mail == null || !Boolean.TRUE.equals(verificado)) {
            response.sendRedirect("/login?error");
            return;
        }

        Usuario usuario = usuarioRepository.buscarPorMail(mail);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setMail(mail);
            // Clave aleatoria: nadie puede entrar por el formulario con esta cuenta sin cambiarla
            usuario.setClave(passwordEncoder.encode(UUID.randomUUID().toString()));
            usuario.setRol(Rol.USUARIO);
            usuario.setAlta(new Date());
            usuario.setEliminado(false);
            usuarioRepository.save(usuario);
        }

        try {
            // Reutiliza tu lógica: valida baja/eliminado y setea "usuariosession"
            UserDetails ud = userDetailsService.loadUserByUsername(mail);
            UsernamePasswordAuthenticationToken auth =
                    UsernamePasswordAuthenticationToken.authenticated(ud, null, ud.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            ((org.springframework.security.core.context.SecurityContext) context).setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response); // obligatorio en Spring Security 6

            response.sendRedirect("/mascota/mis-mascotas");
        } catch (UsernameNotFoundException e) {
            response.sendRedirect("/login?error");
        }
    }
}
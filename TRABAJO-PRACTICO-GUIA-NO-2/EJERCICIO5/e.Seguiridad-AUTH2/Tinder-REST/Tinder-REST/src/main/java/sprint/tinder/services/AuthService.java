package sprint.tinder.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sprint.tinder.config.JwtService;
import sprint.tinder.dtos.AuthRequest;
import sprint.tinder.dtos.AuthResponse;
import sprint.tinder.dtos.RegisterRequest;
import sprint.tinder.entities.Usuario;
import sprint.tinder.repositories.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findAll().stream()
            .anyMatch(usuario -> request.getMail().equals(usuario.getMail()))) {
            throw new IllegalArgumentException("El email ingresado ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setMail(request.getMail());
        usuario.setClave(passwordEncoder.encode(request.getClave()));

        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getMail());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getMail(),
                        request.getClave()
                )
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.getMail());
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
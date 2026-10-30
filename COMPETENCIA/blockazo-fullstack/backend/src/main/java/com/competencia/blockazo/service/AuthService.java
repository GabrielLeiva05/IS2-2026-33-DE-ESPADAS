package com.competencia.blockazo.service;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.AuthResponse;
import com.competencia.blockazo.model.dto.LoginRequest;
import com.competencia.blockazo.model.dto.RegistroRequest;
import com.competencia.blockazo.model.dto.UsuarioResponse;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/** Registro y login (JWT). */
@Service
public class AuthService {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioService usuarioService,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Se da de alta una persona -> queda logueada (devuelve el token). */
    public AuthResponse registrar(RegistroRequest request) throws ErrorServiceException {
        Usuario usuario = usuarioService.registrar(request);
        return respuesta(usuario);
    }

    /** Login con correo y clave. Si las credenciales son invalidas lanza BadCredentialsException (401). */
    public AuthResponse login(LoginRequest request) throws ErrorServiceException {
        String correo = request.correo().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, request.clave()));
        return respuesta(usuarioService.buscarPorCorreo(correo));
    }

    public AuthResponse respuesta(Usuario usuario) {
        return new AuthResponse(jwtService.generarToken(usuario), UsuarioResponse.de(usuario));
    }
}

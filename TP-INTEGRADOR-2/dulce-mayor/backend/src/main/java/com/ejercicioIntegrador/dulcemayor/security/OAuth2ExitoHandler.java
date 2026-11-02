package com.ejercicioIntegrador.dulcemayor.security;

import com.ejercicioIntegrador.dulcemayor.dto.IdentidadExternaDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Se ejecuta cuando Google o Facebook confirman la identidad (adaptado de OAuth2LoginSuccessHandler de Blockazo).
 * 1) Lee la identidad (correo, verificación y nombre) según el proveedor.
 * 2) ServicioAutenticacion aplica las reglas: solo clientes, sin duplicar cuentas.
 * 3) Redirige al sitio institucional con un código de un solo uso (no con el JWT).
 * Se registra como bean solo si hay algún proveedor configurado (ver SeguridadOAuth2Config).
 */
public class OAuth2ExitoHandler implements AuthenticationSuccessHandler {

    private final ServicioAutenticacion servicioAutenticacion;
    private final String redireccionCliente;

    public OAuth2ExitoHandler(ServicioAutenticacion servicioAutenticacion, String redireccionCliente) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.redireccionCliente = redireccionCliente;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            IdentidadExternaDTO identidad = leerIdentidad(authentication);
            Usuario usuario = servicioAutenticacion.ingresarConRedSocial(identidad);
            String codigo = servicioAutenticacion.emitirCodigoCanje(usuario);
            redirigir(request, response, "codigo", codigo);
        } catch (MiException e) {
            redirigir(request, response, "error", e.getMessage());
        }
    }

    static IdentidadExternaDTO leerIdentidad(Authentication authentication) throws MiException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            throw new MiException("No se pudo leer la identidad del proveedor externo");
        }
        OAuth2User usuario = token.getPrincipal();
        String registro = token.getAuthorizedClientRegistrationId();
        String correo = usuario.getAttribute("email");
        if ("google".equalsIgnoreCase(registro)) {
            Object verificado = usuario.getAttribute("email_verified");
            return new IdentidadExternaDTO(correo, Boolean.parseBoolean(String.valueOf(verificado)),
                    usuario.getAttribute("given_name"), usuario.getAttribute("family_name"),
                    ProveedorAutenticacion.GOOGLE);
        }
        if ("facebook".equalsIgnoreCase(registro)) {
            // Facebook no informa si el correo está verificado: se registra como no verificado.
            String nombreCompleto = usuario.getAttribute("name");
            String nombre = null;
            String apellido = null;
            if (nombreCompleto != null && !nombreCompleto.isBlank()) {
                String[] partes = nombreCompleto.trim().split("\\s+", 2);
                nombre = partes[0];
                apellido = partes.length > 1 ? partes[1] : null;
            }
            return new IdentidadExternaDTO(correo, false, nombre, apellido, ProveedorAutenticacion.FACEBOOK);
        }
        throw new MiException("Proveedor de autenticación no soportado: " + registro);
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response, String parametro, String valor)
            throws IOException {
        limpiarSesion(request);
        response.sendRedirect(UriComponentsBuilder.fromUriString(redireccionCliente)
                .queryParam(parametro, valor)
                .build().encode().toUriString());
    }

    /** La API es stateless: la sesión solo se usó durante el intercambio OAuth2. */
    private static void limpiarSesion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}

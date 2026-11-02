package com.dulcemayor.admin.seguridad;

import com.dulcemayor.admin.dto.Roles;
import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.dto.UsuarioSesionApiDTO;

import java.io.Serial;
import java.io.Serializable;

/**
 * Sesión del empleado guardada en la HttpSession del servidor de admin-web.
 * El JWT queda del lado del servidor: el navegador solo recibe la cookie de sesión (HttpOnly).
 * Los permisos reales los decide la API en cada pedido; esta clase solo adapta la navegación.
 */
public final class SesionUsuario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String token;
    private final String correo;
    private final String rol;
    private final String nombre;
    private final String apellido;
    private final boolean debeCambiarClave;

    private SesionUsuario(String token, String correo, String rol, String nombre, String apellido,
                          boolean debeCambiarClave) {
        this.token = token;
        this.correo = correo;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.debeCambiarClave = debeCambiarClave;
    }

    public static SesionUsuario desde(SesionApiDTO sesion) {
        UsuarioSesionApiDTO usuario = sesion.getUsuario();
        return new SesionUsuario(sesion.getToken(), usuario.getCorreo(), usuario.getRol(), usuario.getNombre(),
                usuario.getApellido(), usuario.isDebeCambiarClave());
    }

    public String getToken() {
        return token;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }

    public String getRolLegible() {
        return Roles.legible(rol);
    }

    public String getNombreCompleto() {
        String completo = ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
        return completo.isEmpty() ? correo : completo;
    }

    public String getIniciales() {
        String n = nombre == null || nombre.isBlank() ? "" : nombre.substring(0, 1);
        String a = apellido == null || apellido.isBlank() ? "" : apellido.substring(0, 1);
        String iniciales = (n + a).toUpperCase();
        return iniciales.isEmpty() ? "DM" : iniciales;
    }

    public boolean isDebeCambiarClave() {
        return debeCambiarClave;
    }

    public boolean isJefe() {
        return Roles.JEFE.equals(rol);
    }

    /** El jefe hereda los permisos del administrativo (igual que la jerarquía de roles de la API). */
    public boolean isAdministrativo() {
        return Roles.ADMINISTRATIVO.equals(rol) || isJefe();
    }

    public boolean isPreventista() {
        return Roles.PREVENTISTA.equals(rol);
    }

    public boolean isDistribuidor() {
        return Roles.DISTRIBUIDOR.equals(rol);
    }

    /** Catálogo de solo lectura: administración y preventistas (como GET /api/productos en la API). */
    public boolean isPuedeVerCatalogo() {
        return isAdministrativo() || isPreventista();
    }
}

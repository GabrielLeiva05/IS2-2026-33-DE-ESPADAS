package com.dulcemayor.institucional.seguridad;

import com.dulcemayor.institucional.dto.SesionApiDTO;
import com.dulcemayor.institucional.dto.UsuarioSesionApiDTO;

import java.io.Serial;
import java.io.Serializable;

/**
 * Sesión del cliente guardada en la HttpSession del sitio institucional.
 * El JWT queda en el servidor; el navegador solo tiene la cookie de sesión (HttpOnly).
 */
public final class SesionCliente implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String token;
    private final String correo;
    private final String nombre;
    private final String apellido;
    private final String proveedorAutenticacion;
    /** Cuenta creada por el preventista con la clave temporal: debe cambiarla antes de usar el sitio. */
    private final boolean debeCambiarClave;

    private SesionCliente(String token, String correo, String nombre, String apellido, String proveedorAutenticacion,
                          boolean debeCambiarClave) {
        this.token = token;
        this.correo = correo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.proveedorAutenticacion = proveedorAutenticacion;
        this.debeCambiarClave = debeCambiarClave;
    }

    public static SesionCliente desde(SesionApiDTO sesion) {
        UsuarioSesionApiDTO usuario = sesion.getUsuario();
        return new SesionCliente(sesion.getToken(), usuario.getCorreo(), usuario.getNombre(), usuario.getApellido(),
                usuario.getProveedorAutenticacion(), usuario.isDebeCambiarClave());
    }

    /** Copia con nombre y apellido actualizados (después de editar "Mis datos"). */
    public SesionCliente conNombre(String nuevoNombre, String nuevoApellido) {
        return new SesionCliente(token, correo, nuevoNombre, nuevoApellido, proveedorAutenticacion, debeCambiarClave);
    }

    public boolean isDebeCambiarClave() {
        return debeCambiarClave;
    }

    public String getToken() {
        return token;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombre() {
        return nombre == null || nombre.isBlank() ? correo : nombre;
    }

    public String getNombreCompleto() {
        String completo = ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
        return completo.isEmpty() ? correo : completo;
    }

    public String getIniciales() {
        String n = nombre == null || nombre.isBlank() ? "" : nombre.substring(0, 1);
        String a = apellido == null || apellido.isBlank() ? "" : apellido.substring(0, 1);
        String iniciales = (n + a).toUpperCase();
        return iniciales.isEmpty() ? correo.substring(0, 1).toUpperCase() : iniciales;
    }

    public String getProveedorAutenticacion() {
        return proveedorAutenticacion;
    }

    /** Las cuentas creadas con Google o Facebook no tienen contraseña local que cambiar. */
    public boolean isTieneClaveLocal() {
        return proveedorAutenticacion == null || "LOCAL".equals(proveedorAutenticacion);
    }

    public String getProveedorLegible() {
        if ("GOOGLE".equals(proveedorAutenticacion)) {
            return "Google";
        }
        if ("FACEBOOK".equals(proveedorAutenticacion)) {
            return "Facebook";
        }
        return "Correo y contraseña";
    }
}

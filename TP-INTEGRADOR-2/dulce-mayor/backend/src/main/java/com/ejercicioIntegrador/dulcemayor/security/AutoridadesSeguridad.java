package com.ejercicioIntegrador.dulcemayor.security;

/** Nombres de autoridades usados por la configuración de seguridad. */
public final class AutoridadesSeguridad {

    /**
     * Única autoridad que recibe un usuario con debeCambiarClave = true. Como no es un rol,
     * cualquier endpoint que exija un rol responde 403 hasta que cambie la clave (HU-06).
     */
    public static final String CAMBIO_CLAVE_PENDIENTE = "CAMBIO_CLAVE_PENDIENTE";

    /** Jerarquía del enunciado: el jefe tiene los permisos del administrativo y del preventista. */
    public static final String JERARQUIA_ROLES = """
            ROLE_JEFE > ROLE_ADMINISTRATIVO
            ROLE_JEFE > ROLE_PREVENTISTA
            """;

    private AutoridadesSeguridad() {
    }
}

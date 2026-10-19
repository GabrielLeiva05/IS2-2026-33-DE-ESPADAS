package com.tienda.carrito.config;

/** Nombres de atributos de sesion / peticion usados para identificar al usuario. */
public final class SesionUsuario {

    /** Atributo de SESION: id del usuario que inicio sesion (lo guarda el login). */
    public static final String ID = "usuarioId";
    public static final String NOMBRE = "usuarioNombre";

    /** Atributo de PETICION: el Usuario ACTUAL tal como esta hoy en la base (lo carga SesionInterceptor en cada request). */
    public static final String ACTUAL = "usuarioActual";

    private SesionUsuario() { }
}

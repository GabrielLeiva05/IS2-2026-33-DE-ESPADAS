package com.is2.tinder.audit;

public class UsuarioActualHolder {

    private static final ThreadLocal<String> USUARIO_ID = new ThreadLocal<>();

    private UsuarioActualHolder() {}

    public static void set(String usuarioId) {
        USUARIO_ID.set(usuarioId);
    }

    public static String get() {
        return USUARIO_ID.get();
    }

    public static void limpiar() {
        USUARIO_ID.remove();
    }
}
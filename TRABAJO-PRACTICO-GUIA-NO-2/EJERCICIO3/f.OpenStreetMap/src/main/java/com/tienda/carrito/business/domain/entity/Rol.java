package com.tienda.carrito.business.domain.entity;

/** Roles del sistema. ADMIN accede a la administracion (ABM); CLIENTE solo usa la tienda y su carrito. */
public enum Rol {

    ADMIN("Administrador"),
    CLIENTE("Cliente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

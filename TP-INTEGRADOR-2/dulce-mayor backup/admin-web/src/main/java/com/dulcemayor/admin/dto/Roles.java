package com.dulcemayor.admin.dto;

import java.util.List;

/** Nombres legibles de los roles y roles que se pueden asignar desde admin-web. */
public final class Roles {

    public static final String JEFE = "JEFE";
    public static final String ADMINISTRATIVO = "ADMINISTRATIVO";
    public static final String PREVENTISTA = "PREVENTISTA";
    public static final String DISTRIBUIDOR = "DISTRIBUIDOR";
    public static final String CLIENTE = "CLIENTE";

    /** El jefe puede dar de alta todos los roles de empleado menos JEFE (la API también lo controla). */
    public static final List<String> ASIGNABLES = List.of(ADMINISTRATIVO, PREVENTISTA, DISTRIBUIDOR);

    private Roles() {
    }

    public static String legible(String rol) {
        if (rol == null) {
            return "";
        }
        return switch (rol) {
            case JEFE -> "Jefe administrativo";
            case ADMINISTRATIVO -> "Administrativo";
            case PREVENTISTA -> "Preventista";
            case DISTRIBUIDOR -> "Distribuidor";
            case CLIENTE -> "Cliente";
            default -> rol;
        };
    }
}

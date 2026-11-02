package com.dulcemayor.admin.dto;

/** Cantidad de empleados activos de un rol, para la barra del panel del jefe. */
public record ConteoRolDTO(String rol, long cantidad, int porcentaje) {

    public String getRol() { return rol; }
    public String getRolLegible() { return Roles.legible(rol); }
    public long getCantidad() { return cantidad; }
    public int getPorcentaje() { return porcentaje; }
}

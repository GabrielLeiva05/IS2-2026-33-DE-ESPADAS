package com.is2.tinder.dtos;

public class ReporteVotosDTO {
    private final String nombreUsuario;
    private final String apellidoUsuario;
    private final String nombreMascota;
    private final long cantidadVotos;

    public ReporteVotosDTO(String nombreUsuario, String apellidoUsuario, String nombreMascota, long cantidadVotos) {
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.nombreMascota = nombreMascota;
        this.cantidadVotos = cantidadVotos;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getApellidoUsuario() { return apellidoUsuario; }
    public String getNombreMascota() { return nombreMascota; }
    public long getCantidadVotos() { return cantidadVotos; }
}
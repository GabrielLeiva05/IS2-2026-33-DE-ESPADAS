package com.ejercicioIntegrador.dulcemayor.dto;

public record SucursalRequestDTO(String nombre, String telefono, String emailContacto, String horario,
                                 DireccionRequestDTO direccion, Boolean activa) {
}

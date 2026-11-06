package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

public record PuntoVentaRequestDTO(
        String nombreCliente,
        String apellidoCliente,
        String correoContactoCliente,
        LocalDate fechaNacimientoCliente,
        TipoDocumento tipoDocumentoCliente,
        String numeroDocumentoCliente,
        String nombreComercial,
        String telefono,
        String emailContacto,
        DireccionRequestDTO direccion) {
}

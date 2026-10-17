package com.incisog.Clinica.logic.strategy;

import com.incisog.Clinica.models.DetalleHistoriaClinica;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component("desde")
public class CriterioDesdeFecha implements CriterioBusquedaDetalle{

    @Override
    public boolean cumple(DetalleHistoriaClinica detalle, String valor) {
        LocalDate fechaDetalle = detalle.getFechaHistoria();
        LocalDate fechaRecibida;
        try {
            fechaRecibida = LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener el formato AAAA-MM-DD");
        }
        return !fechaDetalle.isBefore(fechaRecibida);   // "no es anterior" = igual o posterior
    }
}

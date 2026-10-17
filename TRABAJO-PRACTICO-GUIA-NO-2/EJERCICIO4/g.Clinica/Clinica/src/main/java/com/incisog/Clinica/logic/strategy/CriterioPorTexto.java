package com.incisog.Clinica.logic.strategy;

import com.incisog.Clinica.models.DetalleHistoriaClinica;
import org.springframework.stereotype.Component;

@Component("texto")
public class CriterioPorTexto implements CriterioBusquedaDetalle{

    @Override
    public boolean cumple(DetalleHistoriaClinica detalle, String valor) {
        String observacion = detalle.getDetalleHistoria();
        if (observacion == null) {
            return false;
        }
        return observacion.toLowerCase().contains(valor.toLowerCase());
    }
}

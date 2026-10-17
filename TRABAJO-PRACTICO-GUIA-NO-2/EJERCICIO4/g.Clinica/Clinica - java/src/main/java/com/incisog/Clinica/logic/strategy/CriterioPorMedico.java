package com.incisog.Clinica.logic.strategy;

import com.incisog.Clinica.models.DetalleHistoriaClinica;
import com.incisog.Clinica.models.Medico;
import org.springframework.stereotype.Component;

@Component("medico")
public class CriterioPorMedico implements CriterioBusquedaDetalle{
    @Override
    public boolean cumple(DetalleHistoriaClinica detalle, String valor) {
        Medico med = detalle.getMedico();

        if (med == null){
            return false;
        }
        //No es posible tener detalles con Medico null, si eso es una prueba de validacion
        String nombre = med.getNombre() + " " + med.getApellido();
        return nombre.toLowerCase().contains(valor.toLowerCase());
    }
}

package com.incisog.Clinica.logic.strategy;

import com.incisog.Clinica.models.DetalleHistoriaClinica;

//Implementacion de Strategy para la busqueda de detalles de historia clinica segun un criterio
public interface CriterioBusquedaDetalle {
    boolean cumple(DetalleHistoriaClinica detalle, String valor);
}

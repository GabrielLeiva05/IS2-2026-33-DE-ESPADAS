package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.model.TipoPago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFormaDePago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class ServicioFormaDePago {

    @Autowired
    private RepositorioFormaDePago formaDePagoRepository;

    public void crearFormaDePago(TipoPago tipoPago, String observacion) throws Exception {
        try {
            FormaDePago formaPago = new FormaDePago();
            formaPago.setTipoPago(tipoPago);
            formaPago.setObservacion(observacion);
            formaPago.setEliminado(false);
            formaDePagoRepository.save(formaPago);

        } catch (Exception e) {
            throw new Exception("Error al crear la forma de pago: " + e.getMessage());
        }
    }

    public void modificarFormaDePago(String id, TipoPago tipoPago, String observacion) throws Exception{
        try {
            Optional<FormaDePago> formaPago = formaDePagoRepository.findById(id);
            if (formaPago.isPresent()) {
                FormaDePago formaPagoActual = formaPago.get();
                formaPagoActual.setTipoPago(tipoPago);
                formaPagoActual.setObservacion(observacion);
                formaDePagoRepository.save(formaPagoActual);
            } else {
                throw new Exception("Forma de pago no encontrada");
            }
        } catch (Exception e) {
            throw new Exception("Error al modificar la forma de pago: " + e.getMessage());
        }
    }

    public void eliminarFormaDePago(String id) throws Exception{
        try {
            Optional<FormaDePago> formaPago = formaDePagoRepository.findById(id);
            if (formaPago.isPresent()) {
                FormaDePago formaPagoActual = formaPago.get();
                formaPagoActual.setEliminado(true);
                formaDePagoRepository.save(formaPagoActual);
            } else {
                throw new Exception("Forma de pago no encontrada");
            }
        } catch (Exception e) {
            throw new Exception("Error al buscar la forma de pago: " + e.getMessage());
        }
    }


    public Collection<FormaDePago> listarFormaDePago(){
        return formaDePagoRepository.findAll();
    }

    public Collection<FormaDePago> listarFormaDePagoActivo(){
    return formaDePagoRepository.findAll().stream().filter(f -> !f.isEliminado()).toList();
    }
    }



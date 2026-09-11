package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.repository.DireccionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DireccionServicio {

    private final DireccionRepositorio direccionRepositorio;

    public DireccionServicio(DireccionRepositorio direccionRepositorio) {
        this.direccionRepositorio = direccionRepositorio;
    }

    @Transactional
    public Direccion crearDireccion(Pais pais,
                                    Provincia provincia,
                                    Departamento departamento,
                                    Localidad localidad,
                                    String codigoPostal,
                                    String barrio,
                                    String direccion,
                                    String manzanaPiso,
                                    String referencia) throws MiException {

        validar(pais, provincia, departamento, localidad, codigoPostal, barrio, direccion);

        Direccion nuevaDireccion = new Direccion();
        nuevaDireccion.setLocalidad(localidad);
        nuevaDireccion.setBarrio(barrio);
        nuevaDireccion.setManzanaPiso(manzanaPiso);
        nuevaDireccion.setReferencia(referencia); // Referencia queda opcional

        return direccionRepositorio.save(nuevaDireccion);
    }

    private void validar(Pais pais, Provincia provincia, Departamento departamento,
                         Localidad localidad, String codigoPostal, String barrio, String direccion) throws MiException {
        if (pais == null) {
            throw new MiException("Debe seleccionar un país");
        }
        if (provincia == null) {
            throw new MiException("Debe seleccionar una provincia");
        }
        if (departamento == null) {
            throw new MiException("Debe seleccionar un departamento");
        }
        if (localidad == null) {
            throw new MiException("Debe seleccionar una localidad");
        }
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            throw new MiException("El código postal no puede estar vacío");
        }
        if (barrio == null || barrio.trim().isEmpty()) {
            throw new MiException("El barrio no puede estar vacío");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new MiException("La dirección no puede estar vacía");
        }
    }
}
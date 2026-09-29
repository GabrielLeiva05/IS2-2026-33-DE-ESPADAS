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
                                    String calle,
                                    String numeracion,
                                    String manzanaPiso,
                                    String casaDepartamento,
                                    String referencia) throws MiException {

        validar(pais, provincia, departamento, localidad, codigoPostal, barrio, calle, numeracion);

        Direccion nuevaDireccion = new Direccion();
        nuevaDireccion.setLocalidad(localidad);
        nuevaDireccion.setCodigoPostal(codigoPostal.trim());
        nuevaDireccion.setBarrio(barrio);
        nuevaDireccion.setCalle(calle.trim());
        nuevaDireccion.setNumeracion(numeracion.trim());
        nuevaDireccion.setManzanaPiso(manzanaPiso);
        nuevaDireccion.setCasaDepartamento(casaDepartamento);
        nuevaDireccion.setReferencia(referencia); // Referencia queda opcional

        return nuevaDireccion;
    }

    @Transactional
    public Direccion guardarDireccionPerfil(com.ejercicioIntegrador.tiendaderopa.model.Persona persona,
            Localidad localidad, String codigoPostal, String barrio, String calle, String numeracion,
            String manzanaPiso, String casaDepartamento, String referencia) {
        Direccion direccion = persona.getDirecciones().stream()
                .filter(actual -> !actual.isEliminado())
                .findFirst()
                .orElseGet(Direccion::new);
        direccion.setPersona(persona);
        direccion.setLocalidad(localidad);
        direccion.setCodigoPostal(codigoPostal.trim());
        direccion.setBarrio(barrio.trim());
        direccion.setCalle(calle.trim());
        direccion.setNumeracion(numeracion.trim());
        direccion.setManzanaPiso(manzanaPiso);
        direccion.setCasaDepartamento(casaDepartamento);
        direccion.setReferencia(referencia);
        if (!persona.getDirecciones().contains(direccion)) {
            persona.getDirecciones().add(direccion);
        }
        return direccionRepositorio.save(direccion);
    }

    private void validar(Pais pais, Provincia provincia, Departamento departamento,
                         Localidad localidad, String codigoPostal, String barrio, String calle,
                         String numeracion) throws MiException {
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
        if (calle == null || calle.trim().isEmpty()) {
            throw new MiException("La calle no puede estar vacía");
        }
        if (numeracion == null || numeracion.trim().isEmpty()) {
            throw new MiException("La numeración no puede estar vacía");
        }
    }
}
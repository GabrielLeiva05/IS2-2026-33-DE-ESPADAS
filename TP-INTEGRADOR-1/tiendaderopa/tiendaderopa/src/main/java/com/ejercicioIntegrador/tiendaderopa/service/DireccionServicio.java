package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.repository.DireccionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DireccionServicio {

    private final DireccionRepositorio direccionRepositorio;

    public DireccionServicio(DireccionRepositorio direccionRepositorio) {
        this.direccionRepositorio = direccionRepositorio;
    }

    // 1. CREAR / REGISTRAR
    @Transactional
    public Direccion crearDireccion(String calle,
                                    String numeracion,
                                    String barrio,
                                    String manzanaPiso,
                                    String casaDepartamento,
                                    String referencia,
                                    Localidad localidad,
                                    Persona persona) throws MiException {

        validar(calle, numeracion, localidad, persona);

        Direccion nuevaDireccion = new Direccion();
        nuevaDireccion.setCalle(calle.trim());
        nuevaDireccion.setNumeracion(numeracion.trim());
        nuevaDireccion.setBarrio(barrio != null ? barrio.trim() : null);
        nuevaDireccion.setManzanaPiso(manzanaPiso != null ? manzanaPiso.trim() : null);
        nuevaDireccion.setCasaDepartamento(casaDepartamento != null ? casaDepartamento.trim() : null);
        nuevaDireccion.setReferencia(referencia != null ? referencia.trim() : null);
        nuevaDireccion.setLocalidad(localidad);
        nuevaDireccion.setPersona(persona);
        nuevaDireccion.setEliminado(false);

        return direccionRepositorio.save(nuevaDireccion);
    }

    // 2. MODIFICAR / ACTUALIZAR
    @Transactional
    public Direccion modificarDireccion(String id,
                                        String calle,
                                        String numeracion,
                                        String barrio,
                                        String manzanaPiso,
                                        String casaDepartamento,
                                        String referencia,
                                        Localidad localidad,
                                        Persona persona) throws MiException {

        validar(calle, numeracion, localidad, persona);

        Direccion direccion = buscarPorId(id);
        direccion.setCalle(calle.trim());
        direccion.setNumeracion(numeracion.trim());
        direccion.setBarrio(barrio != null ? barrio.trim() : null);
        direccion.setManzanaPiso(manzanaPiso != null ? manzanaPiso.trim() : null);
        direccion.setCasaDepartamento(casaDepartamento != null ? casaDepartamento.trim() : null);
        direccion.setReferencia(referencia != null ? referencia.trim() : null);
        direccion.setLocalidad(localidad);
        direccion.setPersona(persona);

        return direccionRepositorio.save(direccion);
    }

    // 3. BUSCAR POR ID
    @Transactional(readOnly = true)
    public Direccion buscarPorId(String id) throws MiException {
        if (id == null || id.trim().isEmpty()) {
            throw new MiException("El ID de la dirección no puede ser nulo.");
        }
        Optional<Direccion> respuesta = direccionRepositorio.findById(id);
        if (respuesta.isPresent()) {
            return respuesta.get();
        }
        throw new MiException("No se encontró la dirección solicitada.");
    }

    // 4. LISTAR TODAS
    @Transactional(readOnly = true)
    public List<Direccion> listarTodas() {
        return direccionRepositorio.findAll();
    }

    // 5. BAJA LÓGICA (O FÍSICA)
    @Transactional
    public void eliminar(String id) throws MiException {
        Direccion direccion = buscarPorId(id);
        direccion.setEliminado(true);
        direccionRepositorio.save(direccion);
    }

    // VALIDACIÓN DE CAMPOS OBLIGATORIOS SEGÚN @Column(nullable = false)
    private void validar(String calle, String numeracion, Localidad localidad, Persona persona) throws MiException {
        if (calle == null || calle.trim().isEmpty()) {
            throw new MiException("El nombre de la calle no puede estar vacío.");
        }
        if (numeracion == null || numeracion.trim().isEmpty()) {
            throw new MiException("La numeración no puede estar vacía.");
        }
        if (localidad == null) {
            throw new MiException("Debe seleccionar una localidad.");
        }
        if (persona == null) {
            throw new MiException("Debe asociar la dirección a una persona.");
        }
    }
}
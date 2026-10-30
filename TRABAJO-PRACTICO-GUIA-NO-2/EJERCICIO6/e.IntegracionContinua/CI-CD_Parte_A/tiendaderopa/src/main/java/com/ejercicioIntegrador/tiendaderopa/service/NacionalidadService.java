package com.ejercicioIntegrador.tiendaderopa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ejercicioIntegrador.tiendaderopa.model.Nacionalidad;
import com.ejercicioIntegrador.tiendaderopa.repository.NacionalidadRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NacionalidadService {

    private final NacionalidadRepository repository;

    @Transactional
    public Nacionalidad crearNacionalidad(String nombre) {
        validar(nombre);
        Nacionalidad nacionalidad = new Nacionalidad();
        nacionalidad.setNombre(nombre);
        return repository.save(nacionalidad);
    }

    public void validar(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la nacionalidad no puede estar vacío.");
        }
        if (repository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new IllegalStateException("La nacionalidad ya existe.");
        }
    }

    @Transactional(readOnly = true)
    public Nacionalidad buscarNacionalidad(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nacionalidad no encontrada."));
    }

    @Transactional(readOnly = true)
    public Nacionalidad buscarNacionalidadPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RuntimeException("Nacionalidad no encontrada."));
    }

    @Transactional
    public Nacionalidad modificarNacionalidad(String id, String nuevoNombre) {
        Nacionalidad nacionalidad = buscarNacionalidad(id);
        if (!nacionalidad.getNombre().equalsIgnoreCase(nuevoNombre)) {
            validar(nuevoNombre);
        }
        nacionalidad.setNombre(nuevoNombre);
        return repository.save(nacionalidad);
    }

    @Transactional
    public void eliminarNacionalidad(String id) {
        Nacionalidad nacionalidad = buscarNacionalidad(id);
        nacionalidad.setEliminado(true);
        repository.save(nacionalidad);
    }

    @Transactional(readOnly = true)
    public List<Nacionalidad> listarNacionalidad() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Nacionalidad> listarNacionalidadActiva() {
        return repository.findByEliminadoFalse();
    }
}
package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.repositories.DomicilioRepository;
import ar.com.biblioteca.server.repositories.LocalidadRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocalidadService {

    private final LocalidadRepository localidadRepository;
    private final DomicilioRepository domicilioRepository;

    public LocalidadService(LocalidadRepository localidadRepository, DomicilioRepository domicilioRepository) {
        this.localidadRepository = localidadRepository;
        this.domicilioRepository = domicilioRepository;
    }

    @Transactional(readOnly = true)
    public List<Localidad> findAll() {
        return localidadRepository.findAll(Sort.by("denominacion"));
    }

    @Transactional(readOnly = true)
    public Localidad findById(Long id) {
        return localidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la localidad con id " + id));
    }

    @Transactional
    public Localidad create(Localidad localidad) {
        localidad.setId(null);
        return localidadRepository.save(localidad);
    }

    @Transactional
    public Localidad update(Long id, Localidad datos) {
        Localidad existente = findById(id);
        existente.setDenominacion(datos.getDenominacion());
        return localidadRepository.save(existente);
    }

    @Transactional
    public void delete(Long id) {
        Localidad existente = findById(id);
        long enUso = domicilioRepository.countByLocalidadId(id);
        if (enUso > 0) {
            throw new ConflictException("No se puede eliminar la localidad \"" + existente.getDenominacion()
                    + "\" porque está siendo utilizada por " + enUso + " domicilio(s)");
        }
        localidadRepository.delete(existente);
    }
}

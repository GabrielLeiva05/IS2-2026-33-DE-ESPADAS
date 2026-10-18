package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.repositories.DomicilioRepository;
import ar.com.biblioteca.server.repositories.LocalidadRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LocalidadService extends ServicioBase<Localidad> {

    private final DomicilioRepository domicilioRepository;

    public LocalidadService(LocalidadRepository repository, DomicilioRepository domicilioRepository) {
        super(repository);
        this.domicilioRepository = domicilioRepository;
    }

    @Override protected String nombreEntidad() { return "la localidad"; }
    @Override protected Sort ordenamiento() { return Sort.by("denominacion"); }

    @Override
    protected void copiarDatos(Localidad destino, Localidad origen) {
        destino.setDenominacion(origen.getDenominacion());
    }

    @Override
    protected void antesDeEliminar(Localidad existente) {
        long enUso = domicilioRepository.countByLocalidadId(existente.getId());
        if (enUso > 0) {
            throw new ConflictException("No se puede eliminar la localidad \"" + existente.getDenominacion()
                    + "\" porque está siendo utilizada por " + enUso + " domicilio(s)");
        }
    }
}
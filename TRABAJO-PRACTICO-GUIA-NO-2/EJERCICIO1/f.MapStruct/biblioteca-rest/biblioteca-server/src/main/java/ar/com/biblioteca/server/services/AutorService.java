package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LibroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutorService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;

    public AutorService(AutorRepository autorRepository, LibroRepository libroRepository) {
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<Autor> findAll() {
        return autorRepository.findAll(Sort.by("apellido", "nombre"));
    }

    @Transactional(readOnly = true)
    public Autor findById(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el autor con id " + id));
    }

    @Transactional
    public Autor create(Autor autor) {
        autor.setId(null);
        return autorRepository.save(autor);
    }

    @Transactional
    public Autor update(Long id, Autor datos) {
        Autor existente = findById(id);
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setBiografia(datos.getBiografia());
        return autorRepository.save(existente);
    }

    @Transactional
    public void delete(Long id) {
        Autor existente = findById(id);
        long enUso = libroRepository.countByAutoresId(id);
        if (enUso > 0) {
            throw new ConflictException("No se puede eliminar al autor " + existente.getNombre() + " "
                    + existente.getApellido() + " porque está asociado a " + enUso + " libro(s)");
        }
        autorRepository.delete(existente);
    }
}

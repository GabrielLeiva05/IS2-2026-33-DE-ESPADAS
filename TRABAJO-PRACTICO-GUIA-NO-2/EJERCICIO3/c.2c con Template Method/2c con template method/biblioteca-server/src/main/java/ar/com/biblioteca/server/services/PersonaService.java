package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Domicilio;
import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.entities.Persona;
import ar.com.biblioteca.server.exceptions.BadRequestException;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LocalidadRepository;
import ar.com.biblioteca.server.repositories.PersonaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lógica de negocio de Persona y de sus relaciones:
 * Persona 1-1 Domicilio (Domicilio * - 1 Localidad) y Persona (composición) 1-* Libro (Libro *-* Autor).
 *
 * Como los libros están compuestos por la persona (no existen sin ella), su alta, modificación y baja
 * se realizan siempre a través de la Persona propietaria.
 */
@Service
public class PersonaService extends ServicioBase<Persona> {

    private final PersonaRepository personaRepository;
    private final LocalidadRepository localidadRepository;
    private final AutorRepository autorRepository;

    public PersonaService(PersonaRepository personaRepository,
                          LocalidadRepository localidadRepository,
                          AutorRepository autorRepository) {
        super(personaRepository);
        this.personaRepository = personaRepository;
        this.localidadRepository = localidadRepository;
        this.autorRepository = autorRepository;
    }

    @Override
    protected String nombreEntidad() {
        return "la persona";
    }

    @Override
    protected Sort ordenamiento() {
        return Sort.by("apellido", "nombre");
    }

    @Override
    protected List<Persona> buscarPorFiltro(String filtro) {
        String f = filtro.trim();
        return personaRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(f, f, ordenamiento());
    }

    @Override
    protected void antesDeCrear(Persona persona) {
        validarDniDisponible(persona.getDni(), null);
        if (persona.getDomicilio() == null) {
            throw new BadRequestException("El domicilio es obligatorio");
        }

        Domicilio domicilio = persona.getDomicilio();
        domicilio.setId(null);
        domicilio.setLocalidad(resolverLocalidad(domicilio.getLocalidad()));

        List<Libro> libros = persona.getLibros() == null ? new ArrayList<>() : persona.getLibros();
        for (Libro libro : libros) {
            libro.setId(null);
            completarAutores(libro);
        }
        persona.setLibros(libros);
    }

    @Override
    protected void antesDeActualizar(Long id, Persona datos) {
        validarDniDisponible(datos.getDni(), id);
        if (datos.getDomicilio() == null) {
            throw new BadRequestException("El domicilio es obligatorio");
        }
    }

    @Override
    protected void copiarDatos(Persona existente, Persona datos) {
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setDni(datos.getDni());

        Domicilio domicilio = existente.getDomicilio();
        domicilio.setCalle(datos.getDomicilio().getCalle());
        domicilio.setNumero(datos.getDomicilio().getNumero());
        domicilio.setLocalidad(resolverLocalidad(datos.getDomicilio().getLocalidad()));
    }

    // ------------------------------------------------------------------ LIBROS (composición)

    @Transactional(readOnly = true)
    public List<Libro> findLibros(Long personaId) {
        return new ArrayList<>(findById(personaId).getLibros());
    }

    @Transactional(readOnly = true)
    public Libro findLibro(Long personaId, Long libroId) {
        return buscarLibro(findById(personaId), libroId);
    }

    @Transactional
    public Libro createLibro(Long personaId, Libro libro) {
        Persona persona = findById(personaId);
        libro.setId(null);
        completarAutores(libro);
        persona.getLibros().add(libro);
        personaRepository.flush(); // el cascade PERSIST inserta el libro y le asigna el id
        return libro;
    }

    @Transactional
    public Libro updateLibro(Long personaId, Long libroId, Libro datos) {
        Libro existente = buscarLibro(findById(personaId), libroId);
        existente.setTitulo(datos.getTitulo());
        existente.setFecha(datos.getFecha());
        existente.setGenero(datos.getGenero());
        existente.setPaginas(datos.getPaginas());
        existente.setAutor(datos.getAutor());
        existente.getAutores().clear();
        existente.getAutores().addAll(resolverAutores(datos.getAutores()));
        rellenarTextoAutor(existente);
        personaRepository.flush();
        return existente;
    }

    @Transactional
    public void deleteLibro(Long personaId, Long libroId) {
        Persona persona = findById(personaId);
        Libro libro = buscarLibro(persona, libroId);
        persona.getLibros().remove(libro); // orphanRemoval elimina el libro
        personaRepository.flush();
    }

    // ------------------------------------------------------------------ AUXILIARES

    private Libro buscarLibro(Persona persona, Long libroId) {
        return persona.getLibros().stream()
                .filter(l -> l.getId().equals(libroId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La persona " + persona.getId() + " no tiene un libro con id " + libroId));
    }

    private void validarDniDisponible(int dni, Long idPropio) {
        boolean repetido = (idPropio == null)
                ? personaRepository.existsByDni(dni)
                : personaRepository.existsByDniAndIdNot(dni, idPropio);
        if (repetido) {
            throw new ConflictException("Ya existe una persona con el DNI " + dni);
        }
    }

    /** Reemplaza la referencia recibida en el JSON (solo con id) por la Localidad persistida. */
    private Localidad resolverLocalidad(Localidad referencia) {
        if (referencia == null || referencia.getId() == null) {
            throw new BadRequestException("Debe indicar el id de la localidad del domicilio");
        }
        return localidadRepository.findById(referencia.getId())
                .orElseThrow(() -> new BadRequestException(
                        "La localidad con id " + referencia.getId() + " no existe"));
    }

    /** Reemplaza las referencias de autores (solo con id) por los Autores persistidos. */
    private List<Autor> resolverAutores(List<Autor> referencias) {
        List<Autor> resultado = new ArrayList<>();
        if (referencias == null) {
            return resultado;
        }
        for (Autor referencia : referencias) {
            if (referencia == null || referencia.getId() == null) {
                throw new BadRequestException("Cada autor del libro debe indicar su id");
            }
            Autor autor = autorRepository.findById(referencia.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "El autor con id " + referencia.getId() + " no existe"));
            if (!resultado.contains(autor)) {
                resultado.add(autor);
            }
        }
        return resultado;
    }

    private void completarAutores(Libro libro) {
        libro.setAutores(resolverAutores(libro.getAutores()));
        rellenarTextoAutor(libro);
    }

    /** Si el atributo "autor" (String) viene vacío, se completa con los nombres de los autores asociados. */
    private void rellenarTextoAutor(Libro libro) {
        boolean vacio = libro.getAutor() == null || libro.getAutor().isBlank();
        if (vacio && !libro.getAutores().isEmpty()) {
            libro.setAutor(libro.getAutores().stream()
                    .map(a -> a.getNombre() + " " + a.getApellido())
                    .collect(Collectors.joining(", ")));
        }
    }
}
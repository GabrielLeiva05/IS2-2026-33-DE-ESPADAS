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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lógica de negocio de Persona y de sus relaciones:
 * Persona 1-1 Domicilio (Domicilio * - 1 Localidad) y Persona (composición) 1-* Libro (Libro *-* Autor).
 * <p>
 * Como los libros están compuestos por la persona (no existen sin ella), su alta, modificación y baja
 * se realizan siempre a través de la Persona propietaria.
 */
@Service
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final LocalidadRepository localidadRepository;
    private final AutorRepository autorRepository;
    private final ArchivoPdfService archivoPdfService;

    public PersonaService(PersonaRepository personaRepository,
                          LocalidadRepository localidadRepository,
                          AutorRepository autorRepository,
                          ArchivoPdfService archivoPdfService) {
        this.personaRepository = personaRepository;
        this.localidadRepository = localidadRepository;
        this.autorRepository = autorRepository;
        this.archivoPdfService = archivoPdfService;
    }

    // ------------------------------------------------------------------ PERSONA

    @Transactional(readOnly = true)
    public List<Persona> findAll(String filtro) {
        Sort orden = Sort.by("apellido", "nombre");
        if (filtro == null || filtro.isBlank()) {
            return personaRepository.findAll(orden);
        }
        String f = filtro.trim();
        return personaRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(f, f, orden);
    }

    @Transactional(readOnly = true)
    public Persona findById(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la persona con id " + id));
    }

    @Transactional
    public Persona create(Persona persona) {
        validarDniDisponible(persona.getDni(), null);
        if (persona.getDomicilio() == null) {
            throw new BadRequestException("El domicilio es obligatorio");
        }
        persona.setId(null);

        Domicilio domicilio = persona.getDomicilio();
        domicilio.setId(null);
        domicilio.setLocalidad(resolverLocalidad(domicilio.getLocalidad()));

        List<Libro> libros = persona.getLibros() == null ? new ArrayList<>() : persona.getLibros();
        for (Libro libro : libros) {
            libro.setId(null);
            completarAutores(libro);
        }
        persona.setLibros(libros);

        return personaRepository.save(persona);
    }

    /**
     * Actualiza los datos de la persona y de su domicilio. La lista de libros NO se modifica desde aquí
     * (se administra con los métodos de libros de este servicio).
     */
    @Transactional
    public Persona update(Long id, Persona datos) {
        Persona existente = findById(id);
        validarDniDisponible(datos.getDni(), id);
        if (datos.getDomicilio() == null) {
            throw new BadRequestException("El domicilio es obligatorio");
        }

        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setDni(datos.getDni());

        Domicilio domicilio = existente.getDomicilio();
        domicilio.setCalle(datos.getDomicilio().getCalle());
        domicilio.setNumero(datos.getDomicilio().getNumero());
        domicilio.setLocalidad(resolverLocalidad(datos.getDomicilio().getLocalidad()));

        personaRepository.flush();
        return existente;
    }

    /** Elimina la persona; por composición/cascada también se eliminan su domicilio y sus libros. */
    @Transactional
    public void delete(Long id) {
        Persona existente = findById(id);
        List<String> pdfs = nombresDePdf(existente.getLibros());
        personaRepository.delete(existente);
        eliminarArchivosTrasCommit(pdfs); // los libros se borran en cascada: también sus PDF del disco
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

    /** Alta de libro sin PDF (el PDF puede agregarse luego con {@link #reemplazarPdf}). */
    @Transactional
    public Libro createLibro(Long personaId, Libro libro) {
        return createLibro(personaId, libro, null);
    }

    /**
     * Alta de libro con su PDF opcional. El nombre del archivo incluye el id del libro, por eso el PDF se
     * escribe después de insertar el libro. Si la escritura en disco falla, la excepción deshace la
     * transacción y no queda un libro apuntando a un archivo inexistente.
     */
    @Transactional
    public Libro createLibro(Long personaId, Libro libro, MultipartFile archivo) {
        boolean conPdf = archivo != null && !archivo.isEmpty();
        if (conPdf) {
            archivoPdfService.validar(archivo); // falla rápido (400) antes de tocar la base de datos
        }
        Persona persona = findById(personaId);
        libro.setId(null);
        libro.setArchivoPdf(null); // nunca se confía en lo que venga del cliente
        completarAutores(libro);
        persona.getLibros().add(libro);
        personaRepository.flush(); // el cascade PERSIST inserta el libro y le asigna el id
        if (conPdf) {
            libro.setArchivoPdf(archivoPdfService.guardar(archivo, libro.getTitulo(), libro.getId()));
            personaRepository.flush();
        }
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
        String pdf = libro.getArchivoPdf();
        persona.getLibros().remove(libro); // orphanRemoval elimina el libro
        personaRepository.flush();
        eliminarArchivosTrasCommit(pdf == null ? List.of() : List.of(pdf));
    }

    // ------------------------------------------------------------------ PDF DEL LIBRO

    /** Asocia (o reemplaza) el PDF de un libro existente. */
    @Transactional
    public Libro reemplazarPdf(Long personaId, Long libroId, MultipartFile archivo) {
        archivoPdfService.validar(archivo);
        Libro libro = buscarLibro(findById(personaId), libroId);
        String anterior = libro.getArchivoPdf();
        String nuevo = archivoPdfService.guardar(archivo, libro.getTitulo(), libro.getId());
        libro.setArchivoPdf(nuevo);
        personaRepository.flush();
        if (anterior != null && !anterior.equals(nuevo)) {
            eliminarArchivosTrasCommit(List.of(anterior)); // el título cambió: se descarta el archivo viejo
        }
        return libro;
    }

    /** Devuelve el PDF del libro para descargarlo/mostrarlo. 404 si el libro no tiene PDF. */
    @Transactional(readOnly = true)
    public Resource cargarPdf(Long personaId, Long libroId) {
        Libro libro = buscarLibro(findById(personaId), libroId);
        if (!libro.isTienePdf()) {
            throw new ResourceNotFoundException("El libro " + libroId + " no tiene un PDF asociado");
        }
        return archivoPdfService.cargar(libro.getArchivoPdf());
    }

    // ------------------------------------------------------------------ AUXILIARES

    private List<String> nombresDePdf(List<Libro> libros) {
        return libros.stream()
                .map(Libro::getArchivoPdf)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Borra archivos del disco SOLO si la transacción de base de datos termina bien (afterCommit).
     * Así, un rollback no deja libros en la base sin su PDF.
     */
    private void eliminarArchivosTrasCommit(List<String> nombres) {
        if (nombres.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    nombres.forEach(archivoPdfService::eliminar);
                }
            });
        } else {
            nombres.forEach(archivoPdfService::eliminar);
        }
    }

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

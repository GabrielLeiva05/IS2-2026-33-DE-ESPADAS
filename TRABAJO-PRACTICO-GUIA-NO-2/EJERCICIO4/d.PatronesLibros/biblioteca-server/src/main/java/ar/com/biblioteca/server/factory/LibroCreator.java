package ar.com.biblioteca.server.factory;

import ar.com.biblioteca.server.builder.LibroBuilder;
import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;

import java.util.List;

/**
 * Patrón creacional FÁBRICA (Factory Method, GoF) — el "Creator". Mismo molde que PersonaCreator
 * de la Clínica.
 * <pre>
 *   LibroCreator (Creator)                  → nuevoLibro() abstracto = FACTORY METHOD
 *     ├─ LibroFisicoCreator  (ConcreteCreator) → new LibroFisico()   (ConcreteProduct)
 *     └─ LibroDigitalCreator (ConcreteCreator) → new LibroDigital()  (ConcreteProduct)
 *   Libro (Product, abstracta)
 * </pre>
 * El resto del sistema (adapter, DataLoader) trabaja con "Libro" y con "un LibroCreator":
 * nunca hace new LibroFisico() / new LibroDigital(). Agregar un tipo nuevo (ej. AudioLibro) es
 * agregar una entidad y un creator, sin tocar el adapter ni el service.
 *
 * @param <T> producto concreto que fabrica este creator
 */
public abstract class LibroCreator<T extends Libro> {

    /** FACTORY METHOD: cada creator concreto decide qué clase de libro se instancia. */
    protected abstract T nuevoLibro();

    /** Clave del tipo que fabrica ("FISICO" / "DIGITAL"): con ella el adapter elige el creator. */
    public abstract String tipo();

    /** Carga el dato propio del tipo (ubicación / formato) desde el DTO. */
    protected abstract void completarDatosPropios(T libro, LibroDTO dto);

    /** Builder cuyo producto lo crea el factory method de ESTE creator. */
    public LibroBuilder<T> builder() {
        return new LibroBuilder<>(this::nuevoLibro);
    }

    /**
     * Crea el libro a partir de lo que llegó por la API (lo usa LibroAdapter.toEntity).
     *
     * @param autores referencias a los autores (solo con id); el service las reemplaza por las reales
     */
    public T crear(LibroDTO dto, List<Autor> autores) {
        return builder()
                .titulo(dto.getTitulo())
                .fecha(valor(dto.getFecha()))
                .genero(dto.getGenero())
                .paginas(valor(dto.getPaginas()))
                .autor(dto.getAutor())
                .fechaDevolucion(dto.getFechaDevolucion())
                .autores(autores)
                .datosPropios(libro -> completarDatosPropios(libro, dto))
                .build();
    }

    private int valor(Integer numero) {
        return numero == null ? 0 : numero; // null → 0: lo rechazan las validaciones (@Min / @Positive)
    }
}

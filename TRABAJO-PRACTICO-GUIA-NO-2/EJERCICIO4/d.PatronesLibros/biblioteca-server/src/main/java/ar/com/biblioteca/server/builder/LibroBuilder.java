package ar.com.biblioteca.server.builder;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.exceptions.BadRequestException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Patrón creacional BUILDER — construye un Libro paso a paso.
 * <p>
 * Problema que resuelve: un libro tiene muchos datos (título, año, género, páginas, autor, autores,
 * fecha de devolución y el dato propio de su tipo). Con un constructor habría que pasarlos todos en
 * orden ("constructor telescópico": new Libro("Ficciones", 1944, "Cuentos", 174, ...) — ¿cuál era cuál?).
 * Con el builder cada dato se nombra, se pueden omitir los opcionales y la validación queda en build():
 * <pre>
 *   Libro libro = creator.builder()
 *           .titulo("Ficciones").fecha(1944).genero("Cuentos").paginas(174)
 *           .autores(borges)
 *           .build();
 * </pre>
 * Cómo se combina con la FÁBRICA: el builder no decide si el libro es físico o digital; recibe una
 * "fábrica" (Supplier) y la llama recién en build(). Esa fábrica es el factory method del LibroCreator.
 * Así: la Fábrica decide QUÉ clase se crea y el Builder decide CÓMO se arma.
 *
 * @param <T> tipo concreto de libro que se construye (LibroFisico o LibroDigital)
 */
public class LibroBuilder<T extends Libro> {

    private final Supplier<T> fabrica;

    // Las "partes" que se van acumulando hasta build()
    private String titulo;
    private int fecha;
    private String genero;
    private int paginas;
    private String autor;
    private LocalDate fechaDevolucion;
    private final List<Autor> autores = new ArrayList<>();
    private Consumer<T> datosPropios = libro -> { };

    public LibroBuilder(Supplier<T> fabrica) {
        this.fabrica = fabrica;
    }

    public LibroBuilder<T> titulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public LibroBuilder<T> fecha(int fecha) {
        this.fecha = fecha;
        return this;
    }

    public LibroBuilder<T> genero(String genero) {
        this.genero = genero;
        return this;
    }

    public LibroBuilder<T> paginas(int paginas) {
        this.paginas = paginas;
        return this;
    }

    public LibroBuilder<T> autor(String autor) {
        this.autor = autor;
        return this;
    }

    public LibroBuilder<T> fechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
        return this;
    }

    public LibroBuilder<T> autores(List<Autor> autores) {
        if (autores != null) {
            this.autores.addAll(autores);
        }
        return this;
    }

    public LibroBuilder<T> autores(Autor... autores) {
        return autores(List.of(autores));
    }

    /**
     * Paso para el dato propio del tipo concreto (ubicación del físico, formato del digital).
     * Ej: fisicoCreator.builder()....datosPropios(l -> l.setUbicacion("Estante A1"))
     */
    public LibroBuilder<T> datosPropios(Consumer<T> datosPropios) {
        this.datosPropios = datosPropios;
        return this;
    }

    /** Arma el libro: valida lo obligatorio, pide la instancia a la fábrica y le carga las partes. */
    public T build() {
        if (titulo == null || titulo.isBlank()) {
            throw new BadRequestException("El título es obligatorio");
        }
        if (genero == null || genero.isBlank()) {
            throw new BadRequestException("El género es obligatorio");
        }

        T libro = fabrica.get();                 // ← factory method: acá se decide la clase concreta
        libro.setTitulo(titulo.trim());
        libro.setFecha(fecha);
        libro.setGenero(genero.trim());
        libro.setPaginas(paginas);
        libro.setAutor(autor);
        libro.setFechaDevolucion(fechaDevolucion);
        libro.setAutores(new ArrayList<>(autores));
        datosPropios.accept(libro);
        return libro;
    }
}

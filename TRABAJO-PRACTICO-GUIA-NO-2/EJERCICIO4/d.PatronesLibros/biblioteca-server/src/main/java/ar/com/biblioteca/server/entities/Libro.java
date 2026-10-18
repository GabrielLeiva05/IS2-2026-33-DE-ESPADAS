package ar.com.biblioteca.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Libro: id: Long, titulo: String, fecha: int, genero: String, paginas: int, autor: String.
 * <p>
 * Relación del diagrama: Libro * -> * Autor (muchos a muchos, unidireccional desde Libro).
 * El atributo "autor: String" se conserva tal como figura en el diagrama (texto de presentación);
 * la relación real con la entidad Autor es la colección "autores".
 * <p>
 * REFACTOR (patrón Fábrica): Libro pasa a ser ABSTRACTA. Los objetos concretos son
 * {@link LibroFisico} y {@link LibroDigital}, y los fabrica un LibroCreator (factory/).
 * Ya no tiene constructor con parámetros: los libros se arman con el LibroBuilder (builder/).
 * <p>
 * Herencia en JPA: SINGLE_TABLE = una sola tabla "libro" para los dos tipos; la columna "tipo"
 * (discriminador) indica si cada fila es FISICO o DIGITAL.
 */
@Entity
@Table(name = "libro")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@SQLDelete(sql = "UPDATE libro SET eliminado = true WHERE id = ?")
@SQLRestriction("eliminado = false")
public abstract class Libro extends EntidadBase {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    /** Año de publicación. */
    @Min(value = 1, message = "El año de publicación debe ser mayor a cero")
    @Max(value = 2100, message = "El año de publicación no puede superar 2100")
    @Column(nullable = false)
    private int fecha;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 100, message = "El género no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String genero;

    @Positive(message = "La cantidad de páginas debe ser mayor a cero")
    @Column(nullable = false)
    private int paginas;

    @Size(max = 300, message = "El campo autor no puede superar los 300 caracteres")
    @Column(length = 300)
    private String autor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Autor> autores = new ArrayList<>();

    private LocalDate fechaDevolucion;

    protected Libro() {
    }

    /** "FISICO" o "DIGITAL". No es un atributo: lo responde cada subclase (sale en el JSON como "tipo"). */
    public abstract String getTipo();

    /** Copia los datos que son propios de cada tipo (ubicación / formato). Lo usa la modificación. */
    public abstract void copiarDatosPropios(Libro origen);

    @Override
    public String descripcion() {
        return titulo + "-por " + autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getFecha() {
        return fecha;
    }

    public void setFecha(int fecha) {
        this.fecha = fecha;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getPaginas() {
        return paginas;
    }

    public void setPaginas(int paginas) {
        this.paginas = paginas;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
}

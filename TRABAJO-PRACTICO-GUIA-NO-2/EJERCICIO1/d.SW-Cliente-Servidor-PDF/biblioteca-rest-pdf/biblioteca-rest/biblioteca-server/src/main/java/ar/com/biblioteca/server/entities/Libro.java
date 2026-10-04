package ar.com.biblioteca.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Libro: id: Long, titulo: String, fecha: int, genero: String, paginas: int, autor: String.
 * <p>
 * Relación del diagrama: Libro * -> * Autor (muchos a muchos, unidireccional desde Libro).
 * El atributo "autor: String" se conserva tal como figura en el diagrama (texto de presentación);
 * la relación real con la entidad Autor es la colección "autores".
 * <p>
 * Archivo PDF: el contenido del PDF NO se guarda en la base de datos sino en disco (ver ArchivoPdfService);
 * en el libro solo se registra el nombre del archivo ({@code archivoPdf}). Hacia la API se expone únicamente
 * el booleano derivado {@code tienePdf}, de modo que ni el nombre ni la ruta del servidor salen a la red
 * y un cliente no puede apuntar un libro a un archivo arbitrario.
 */
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /** Nombre del archivo PDF en el disco del servidor (p. ej. libro_ficciones_2.pdf). Null si no tiene PDF. */
    @JsonIgnore
    @Column(name = "archivo_pdf", length = 255)
    private String archivoPdf;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Autor> autores = new ArrayList<>();

    public Libro() {
    }

    public Libro(String titulo, int fecha, String genero, int paginas) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.genero = genero;
        this.paginas = paginas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getArchivoPdf() {
        return archivoPdf;
    }

    public void setArchivoPdf(String archivoPdf) {
        this.archivoPdf = archivoPdf;
    }

    /** Propiedad derivada para la API: indica si el libro tiene un PDF asociado. Solo lectura. */
    @JsonProperty(value = "tienePdf", access = JsonProperty.Access.READ_ONLY)
    public boolean isTienePdf() {
        return archivoPdf != null && !archivoPdf.isBlank();
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }
}

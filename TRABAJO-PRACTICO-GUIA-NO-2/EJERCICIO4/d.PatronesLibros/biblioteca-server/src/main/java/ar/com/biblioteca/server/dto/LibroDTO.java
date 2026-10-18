package ar.com.biblioteca.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Forma del libro PARA LA API (lo que entra y sale como JSON).
 * <p>
 * Junta en un solo objeto los dos tipos de libro: "tipo" dice cuál es, y solo se usa
 * "ubicacion" (FISICO) o "formato" (DIGITAL). Del DTO a la entidad concreta pasa el LibroAdapter,
 * que le pide a la Fábrica el libro correcto según "tipo".
 * <p>
 * Los nombres de los campos son los mismos que tenía la entidad: el cliente no tiene que cambiar.
 */
public class LibroDTO {

    private Long id;

    /** "FISICO" o "DIGITAL". Si no viene, se toma FISICO (compatible con los pedidos de antes). */
    private String tipo;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1, message = "El año de publicación debe ser mayor a cero")
    @Max(value = 2100, message = "El año de publicación no puede superar 2100")
    private Integer fecha;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 100, message = "El género no puede superar los 100 caracteres")
    private String genero;

    @NotNull(message = "La cantidad de páginas es obligatoria")
    @Positive(message = "La cantidad de páginas debe ser mayor a cero")
    private Integer paginas;

    @Size(max = 300, message = "El campo autor no puede superar los 300 caracteres")
    private String autor;

    private LocalDate fechaDevolucion;

    /** Autores del libro. Al crear/modificar alcanza con el id de cada uno: {"autores":[{"id":1}]} */
    private List<AutorDTO> autores = new ArrayList<>();

    /** Solo libros FÍSICOS: estante / sector donde está guardado. */
    @Size(max = 100, message = "La ubicación no puede superar los 100 caracteres")
    private String ubicacion;

    /** Solo libros DIGITALES: PDF, EPUB, ... */
    @Size(max = 20, message = "El formato no puede superar los 20 caracteres")
    private String formato;

    public LibroDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Integer getFecha() { return fecha; }
    public void setFecha(Integer fecha) { this.fecha = fecha; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public Integer getPaginas() { return paginas; }
    public void setPaginas(Integer paginas) { this.paginas = paginas; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public List<AutorDTO> getAutores() { return autores; }
    public void setAutores(List<AutorDTO> autores) { this.autores = autores; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
}

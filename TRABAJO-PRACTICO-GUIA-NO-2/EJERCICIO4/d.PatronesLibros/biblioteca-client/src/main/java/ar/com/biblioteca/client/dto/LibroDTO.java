package ar.com.biblioteca.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de Libro: id: Long, titulo: String, fecha: int, genero: String, paginas: int, autor: String (+ autores).
 * <p>
 * REFACTOR (Fábrica en el servidor): se agrega "tipo" (FISICO / DIGITAL) y el dato propio de cada uno
 * (ubicacion / formato). El servidor usa "tipo" para elegir qué clase de libro fabricar.
 */
public class LibroDTO {

    public static final String FISICO = "FISICO";
    public static final String DIGITAL = "DIGITAL";

    private Long id;

    /** FISICO o DIGITAL. Por defecto, físico. */
    private String tipo = FISICO;

    /** Solo libros físicos: estante / sector. */
    @Size(max = 100, message = "La ubicación no puede superar los 100 caracteres")
    private String ubicacion;

    /** Solo libros digitales: PDF, EPUB, ... */
    @Size(max = 20, message = "El formato no puede superar los 20 caracteres")
    private String formato;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    /** Año de publicación. */
    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1, message = "El año debe ser mayor a cero")
    @Max(value = 2100, message = "El año no puede superar 2100")
    private Integer fecha;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 100, message = "El género no puede superar los 100 caracteres")
    private String genero;

    @NotNull(message = "La cantidad de páginas es obligatoria")
    @Min(value = 1, message = "Las páginas deben ser mayor a cero")
    private Integer paginas;

    @Size(max = 300, message = "El campo autor no puede superar los 300 caracteres")
    private String autor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDevolucion;

    /** Autores asociados (relación Libro *-* Autor), tal como los devuelve la API. */
    private List<AutorDTO> autores = new ArrayList<>();

    /**
     * Ids de autores tildados en el formulario. Es un dato exclusivo de la vista:
     * no viaja hacia/desde el servidor (@JsonIgnore); antes de enviar se convierte a "autores".
     */
    @JsonIgnore
    private List<Long> autoresIds = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
    /** Para la vista: "Digital (EPUB)" o "Físico (Estante A1)". */
    @JsonIgnore
    public String getTipoTexto() {
        if (DIGITAL.equals(tipo)) {
            return "Digital" + (formato == null || formato.isBlank() ? "" : " (" + formato + ")");
        }
        return "Físico" + (ubicacion == null || ubicacion.isBlank() ? "" : " (" + ubicacion + ")");
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getFecha() {
        return fecha;
    }

    public void setFecha(Integer fecha) {
        this.fecha = fecha;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getPaginas() {
        return paginas;
    }

    public void setPaginas(Integer paginas) {
        this.paginas = paginas;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public List<AutorDTO> getAutores() {
        return autores;
    }

    public void setAutores(List<AutorDTO> autores) {
        this.autores = autores;
    }

    @JsonIgnore
    public List<Long> getAutoresIds() {
        return autoresIds;
    }

    @JsonIgnore
    public void setAutoresIds(List<Long> autoresIds) {
        this.autoresIds = autoresIds;
    }
}

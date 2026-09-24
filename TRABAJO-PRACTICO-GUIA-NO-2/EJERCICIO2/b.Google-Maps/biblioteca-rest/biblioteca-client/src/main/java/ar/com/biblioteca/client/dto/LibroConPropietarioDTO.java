package ar.com.biblioteca.client.dto;

/**
 * DTO exclusivo de la vista "Libros": un libro junto con la persona a la que pertenece
 * (la API expone los libros anidados dentro de cada persona, por la composición del diagrama).
 */
public class LibroConPropietarioDTO {

    private final LibroDTO libro;
    private final PersonaDTO propietario;

    public LibroConPropietarioDTO(LibroDTO libro, PersonaDTO propietario) {
        this.libro = libro;
        this.propietario = propietario;
    }

    public LibroDTO getLibro() {
        return libro;
    }

    public PersonaDTO getPropietario() {
        return propietario;
    }
}

package ar.com.biblioteca.server.factory;

import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.entities.LibroFisico;
import org.springframework.stereotype.Component;

/** ConcreteCreator: fabrica libros en papel. */
@Component
public class LibroFisicoCreator extends LibroCreator<LibroFisico> {

    @Override
    protected LibroFisico nuevoLibro() {
        return new LibroFisico();
    }

    @Override
    public String tipo() {
        return LibroFisico.TIPO;
    }

    @Override
    protected void completarDatosPropios(LibroFisico libro, LibroDTO dto) {
        libro.setUbicacion(dto.getUbicacion());
    }
}

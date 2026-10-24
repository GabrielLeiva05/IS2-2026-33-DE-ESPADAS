package ar.com.biblioteca.server.factory;

import ar.com.biblioteca.server.dto.LibroDTO;
import ar.com.biblioteca.server.entities.LibroDigital;
import org.springframework.stereotype.Component;

/** ConcreteCreator: fabrica libros electrónicos. */
@Component
public class LibroDigitalCreator extends LibroCreator<LibroDigital> {

    @Override
    protected LibroDigital nuevoLibro() {
        return new LibroDigital();
    }

    @Override
    public String tipo() {
        return LibroDigital.TIPO;
    }

    @Override
    protected void completarDatosPropios(LibroDigital libro, LibroDTO dto) {
        libro.setFormato(dto.getFormato() == null ? null : dto.getFormato().trim().toUpperCase());
    }
}

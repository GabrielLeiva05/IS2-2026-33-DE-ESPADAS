package ar.com.biblioteca.server.exceptions;

/** Se lanza ante un conflicto de negocio: DNI repetido, registro en uso, etc. (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}

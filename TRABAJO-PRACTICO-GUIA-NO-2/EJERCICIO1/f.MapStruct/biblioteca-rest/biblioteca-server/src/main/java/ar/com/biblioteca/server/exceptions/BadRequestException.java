package ar.com.biblioteca.server.exceptions;

/** Se lanza cuando la solicitud es inválida a nivel de negocio (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String mensaje) {
        super(mensaje);
    }
}

package ar.com.biblioteca.server.exceptions;

/** Se lanza cuando no existe el recurso pedido (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}

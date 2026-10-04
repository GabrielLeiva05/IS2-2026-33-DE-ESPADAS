package ar.com.biblioteca.server.exceptions;

/**
 * Falla de infraestructura al leer, escribir o borrar un archivo en el disco del servidor (HTTP 500).
 * Al ser una RuntimeException, si ocurre dentro de una transacción provoca el rollback.
 */
public class StorageException extends RuntimeException {

    public StorageException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public StorageException(String mensaje) {
        super(mensaje);
    }
}

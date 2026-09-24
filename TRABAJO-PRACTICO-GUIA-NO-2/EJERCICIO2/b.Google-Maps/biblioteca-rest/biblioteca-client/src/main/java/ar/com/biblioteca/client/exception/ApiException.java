package ar.com.biblioteca.client.exception;

/** Error ocurrido al consumir la API del servidor (respuesta 4xx/5xx o servidor inaccesible). */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

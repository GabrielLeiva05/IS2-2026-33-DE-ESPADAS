package ar.com.biblioteca.server.exceptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cuerpo JSON uniforme que devuelve la API cuando ocurre un error. */
public class ApiError {

    private final String timestamp = Instant.now().toString();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fieldErrors = new LinkedHashMap<>();

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

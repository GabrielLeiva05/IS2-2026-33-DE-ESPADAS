package com.ejercicioIntegrador.dulcemayor.controller.base;

import com.ejercicioIntegrador.dulcemayor.dto.base.ErrorDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.exceptions.ServicioExternoNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduce las excepciones a respuestas HTTP. Pertenece a la capa de controladores y es comun
 * a todos los modulos: los controladores no necesitan try/catch y los servicios no conocen HTTP.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDTO> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorDTO> operacionNoPermitida(OperacionNoPermitidaException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    @ExceptionHandler(ServicioExternoNoDisponibleException.class)
    public ResponseEntity<ErrorDTO> servicioExternoNoDisponible(ServicioExternoNoDisponibleException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(MiException.class)
    public ResponseEntity<ErrorDTO> errorDeNegocio(MiException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> cuerpoInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no tiene un formato válido (revise el JSON y los valores enumerados)");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "El parametro '" + e.getName() + "' tiene un valor inválido");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDTO> archivoDemasiadoGrande(MaxUploadSizeExceededException e) {
        return responder(HttpStatusCode.valueOf(413), "El archivo supera el tamaño máximo permitido");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> accesoDenegado(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
    }

    /** Login con correo o contraseña incorrectos (mensaje genérico: no revela si la cuenta existe). */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTO> credencialesIncorrectas(BadCredentialsException e) {
        return responder(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    /** Cuenta deshabilitada, bloqueada o vencida. */
    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ErrorDTO> cuentaNoHabilitada(AccountStatusException e) {
        return responder(HttpStatus.UNAUTHORIZED, "La cuenta no está habilitada");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> noAutenticado(AuthenticationException e) {
        return responder(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> errorInesperado(Exception e) {
        // Las excepciones propias de Spring MVC (404 de ruta, 405, 415...) ya traen su codigo HTTP
        if (e instanceof ErrorResponse errorResponse) {
            return responder(errorResponse.getStatusCode(), e.getMessage());
        }
        LOG.error("Error no controlado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error de sistema");
    }

    private ResponseEntity<ErrorDTO> responder(HttpStatusCode estado, String mensaje) {
        String error = estado instanceof HttpStatus status ? status.getReasonPhrase() : String.valueOf(estado.value());
        return ResponseEntity.status(estado).body(ErrorDTO.de(estado.value(), error, mensaje));
    }
}

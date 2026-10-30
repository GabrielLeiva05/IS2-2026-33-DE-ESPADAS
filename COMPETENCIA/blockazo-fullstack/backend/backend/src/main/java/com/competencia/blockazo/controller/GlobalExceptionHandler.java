package com.competencia.blockazo.controller;

import com.competencia.blockazo.error.EntidadNoEncontradaException;
import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduce las excepciones a respuestas JSON con el codigo HTTP correcto. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> noEncontrada(EntidadNoEncontradaException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ErrorServiceException.class)
    public ResponseEntity<ErrorResponse> errorNegocio(ErrorServiceException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Hay datos invalidos en el formulario", errores));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion es invalido (revisar formato de fechas: yyyy-MM-dd)");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> deshabilitado(DisabledException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, "El usuario se encuentra dado de baja");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> credenciales(AuthenticationException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Correo o clave incorrectos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e) {
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta accion");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception e) {
        // Errores propios de Spring MVC (404 ruta inexistente, 405 metodo no permitido, etc.)
        if (e instanceof org.springframework.web.ErrorResponse errorSpring) {
            int codigo = errorSpring.getStatusCode().value();
            return ResponseEntity.status(codigo).body(new ErrorResponse(codigo, e.getMessage()));
        }
        log.error("Error no controlado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error de Sistemas");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje));
    }
}

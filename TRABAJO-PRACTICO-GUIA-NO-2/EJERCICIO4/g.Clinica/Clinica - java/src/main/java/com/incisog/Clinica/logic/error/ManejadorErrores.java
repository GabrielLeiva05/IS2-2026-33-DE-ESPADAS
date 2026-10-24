package com.incisog.Clinica.logic.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * annotations = RestController.class: este manejador atiende SOLO a los @RestController (la API
 * que usa React). Los @Controller de vistas atrapan sus errores con try/catch y los muestran
 * en la pantalla; no queremos que un error en una vista termine como texto plano con 400.
 */
@RestControllerAdvice(annotations = RestController.class)
public class ManejadorErrores {

    @ExceptionHandler(ErrorServiceException.class)
    public ResponseEntity<String> devolverMensaje(Exception e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

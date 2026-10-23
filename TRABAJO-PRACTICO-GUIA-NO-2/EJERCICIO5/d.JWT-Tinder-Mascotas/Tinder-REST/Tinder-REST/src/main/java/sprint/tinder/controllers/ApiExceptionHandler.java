package sprint.tinder.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sprint.tinder.dtos.ApiErrorDto;
import sprint.tinder.errors.ErrorServicio;

@RestControllerAdvice(assignableTypes = MascotaApiController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(ErrorServicio.class)
    public ResponseEntity<ApiErrorDto> handleErrorServicio(ErrorServicio exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(exception.getMessage()));
    }
}
